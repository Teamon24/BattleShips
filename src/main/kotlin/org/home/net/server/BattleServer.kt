package org.home.net.server

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.Conditions
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.utils.extensions.className
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerToRemoveReceived
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.contoller.events.ShipWasConstructed
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel
import org.home.mvc.model.battleIsEnded
import org.home.mvc.model.thoseAreReady
import org.home.net.BattleClient.ActionTypeAbsentException
import org.home.net.PlayerSocket
import org.home.net.action.Action
import org.home.net.action.AreReadyAction
import org.home.net.action.BattleEndAction
import org.home.net.action.BattleStartAction
import org.home.net.action.ConnectedPlayersAction
import org.home.net.action.ConnectionAction
import org.home.net.action.DefeatAction
import org.home.net.action.DisconnectAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.FleetsReadinessAction
import org.home.net.action.HasAShot
import org.home.net.action.HitAction
import org.home.net.action.LeaveAction
import org.home.net.action.MissAction
import org.home.net.action.NewServerAction
import org.home.net.action.NewServerConnectionAction
import org.home.net.action.NotReadyAction
import org.home.net.action.PlayerReadinessAction
import org.home.net.action.PlayerToRemoveAction
import org.home.net.action.ReadyAction
import org.home.net.action.ShipConstructionAction
import org.home.net.action.ShipDeletionAction
import org.home.net.action.ShotAction
import org.home.net.action.TurnAction
import org.home.utils.DSLContainer
import org.home.utils.PlayerSocketUtils.send
import org.home.utils.PlayersSocketsExtensions.exclude
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.extensions.CollectionsExtensions.hasElements
import org.home.utils.extensions.CollectionsExtensions.shuffledKeys
import org.home.utils.extensions.ln
import org.home.utils.log
import org.home.utils.logging
import tornadofx.FXEvent


class BattleServer(
    private val gameController: GameTypeController,
    notifierStrategies: ShotNotifierStrategies,
    private val conditions: Conditions,
    processor: MessageProcessor<Action, PlayerSocket>,
    receiver: MessageReceiver<Action, PlayerSocket>,
    accepter: ConnectionsListener<Action, PlayerSocket>,
    appProps: ApplicationProperties,
) : MultiServer<Action, PlayerSocket>(processor, receiver, accepter, appProps) {

    private val Collection<String>.haveNoTurn get() = all { it != turnPlayer }
    private val String.hasTurn get() = this == turnPlayer
    private val String.hasNoTurn get() = this != turnPlayer

    private val shotNotifier = notifierStrategies.create(sockets)

    private val turnList: MutableList<String> = mutableListOf()
    private lateinit var turnPlayer: String

    override fun send(action: Action) = sockets.send(action)
    override fun send(actions: Collection<Action>) = sockets.send(actions)

    override fun process(socket: PlayerSocket, message: Action) {
        val action = message
        when (action) {
            is ConnectionAction -> {
                permitToAccept(sockets.size + 1 < model.playersNumber.value)
                action.also {
                    val connected = it.player

                    socket.player = connected

                    eventbus {
                        +ConnectedPlayerReceived(it)
                    }

                    sockets.exclude(connected).send(it)
                    sockets[connected].send {
                        +FleetSettingsAction(model)
                        +ConnectedPlayersAction(model.playersNames.exclude(connected))
                        +fleetsReadinessExcept(connected, model)
                        +AreReadyAction(model.thoseAreReady)
                    }
                }
            }

            is ReadyAction,
            is NotReadyAction -> processReadiness(action as PlayerReadinessAction)

            is ShipConstructionAction -> {
                sockets.exclude(action.player).send(action)
                eventbus {
                    + ShipWasConstructed(action)
                }
            }

            is ShipDeletionAction -> {
                sockets.exclude(action.player).send(action)
                eventbus {
                    + ShipWasDeleted(action)
                }
            }

            is ShotAction -> {
                val target = action.target

                val serverIsTarget = target == currentPlayer
                if (serverIsTarget) {
                    val shot = action.shot
                    model.registersAHit(shot)
                        .yes {
                            onHit(action)
                            val ships = model.playersAndShips[currentPlayer]!!
                            ships.isEmpty().so { turnList.remove(currentPlayer) }
                            return
                        }
                        .no { onMiss(action) }
                } else {
                    sockets.send(shotNotifier.notifiactions(action))
                    sockets[action.target].send(action)
                }
            }

            is HitAction -> {
                sockets.send(shotNotifier.notifiactions(action))
                eventbus {
                    +ShipWasHit(action)
                }
            }

            is MissAction -> {
                val nextTurn = notifyAboutShotAndSendNextTurn(action)

                eventbus {
                    +ThereWasAMiss(action)
                    +TurnReceived(nextTurn)
                }
            }

            is PlayerToRemoveAction -> sendRemovePlayer(action)
            is NewServerConnectionAction -> conditions.newServerFound.notifyUI() {
                newServer = action.ip to action.port
            }

            else -> throw ActionTypeAbsentException(action.javaClass.name, javaClass.name, "process")
        }
    }

    private val battleIsStarted get() = turnList.isNotEmpty()
    private val battleIsNotEnded get() = turnList.hasElements

    private fun sendRemovePlayer(action: PlayerToRemoveAction) {
        val removedPlayer = action.player
        sockets.exclude(removedPlayer).send(action)

        eventbus {
            when (action) {
                is DisconnectAction -> removeAndFire(removedPlayer, ::PlayerWasDisconnected)
                is LeaveAction -> removeAndFire(removedPlayer, ::PlayerLeaved)
                is DefeatAction -> Unit
            }

            battleIsStarted.so {
                turnList.remove(removedPlayer)

                action.isDefeat?.apply {
                    +PlayerWasDefeated(removedPlayer)
                    battleIsNotEnded.so {

                        removedPlayer.hasTurn.so {
                            turnPlayer = nextTurn()
                            +TurnReceived(turnPlayer)
                            send(TurnAction(turnPlayer))
                        }

                        currentPlayer.hasTurn.yes {
                            +TurnReceived(currentPlayer)
                        } no {
                            action {
                                sockets[shooter].send(TurnAction(shooter))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun DSLContainer<FXEvent>.removeAndFire(removedPlayer: String, event: (String) -> PlayerToRemoveReceived) {
        sockets.removeIf { it.player == removedPlayer }
        + event(removedPlayer)
    }

    private fun onMiss(action: ShotAction) {
        val missAction = MissAction(action)
        val nextTurn = nextTurn()

        send {
            +missAction
            +TurnAction(nextTurn)
        }

        eventbus {
            +ThereWasAMiss(missAction)
            +TurnReceived(nextTurn)
        }
    }


    private fun notifyAboutShotAndSendNextTurn(action: HasAShot): String {
        val playersAndShotMessages = shotNotifier.notifiactions(action)

        val nextTurn = nextTurn()
        val playersAndTurnAction = sockets.associate {
            it.player!! to mutableListOf(TurnAction(nextTurn))
        }

        sockets.send(playersAndShotMessages, playersAndTurnAction)

        return nextTurn
    }

    private fun processReadiness(action: PlayerReadinessAction) {
        sockets.exclude(action.player).send(action)
        gameController.onReady(action)
        logging {
            model.playersReadiness.forEach {
                ln("${it.key}: ${it.value}")
            }
        }
    }

    override fun startBattle() {
        val shuffled = model.playersAndShips.shuffledKeys()
        val player = shuffled.first()

        turnList.addAll(shuffled)
        log { "turn $shuffled" }
        turnPlayer = player

        send {
            +ReadyAction(currentPlayer)
            +BattleStartAction
            +TurnAction(player)
        }

        eventbus {
            +BattleStarted
            +TurnReceived(turnPlayer)
        }
    }


    override fun disconnect() {
        sockets.forEach { it.close() }
        processor.interrupt()
        receiver.interrupt()
        accepter.interrupt()
        serverSocket.close()
    }

    override fun leaveBattle() {
        send(LeaveAction(currentPlayer))

        if (!model.battleIsEnded) {
            TODO("${this.className}#leaveBattle has no server transfer logic that tested for correct implementation")
            sockets.send(NewServerAction(turnPlayer))
            conditions.newServerFound.await()
            sockets.exclude(turnPlayer).send(
                NewServerConnectionAction(
                    turnPlayer,
                    model.newServer.first,
                    model.newServer.second))
        }

        disconnect()
    }

    override fun endBattle() {
        eventbus {
            +BattleIsEnded(BattleEndAction(model.getWinner()))
        }
    }

    override fun connectAndSend(ip: String, port: Int) {
        throw UnsupportedOperationException("${this.className}#connectAndSend")
    }

    override fun onFleetCreationViewExit() {
        leaveBattle()
    }

    override fun accept(): PlayerSocket {
        val socket = PlayerSocket(serverSocket.accept())
        log { "client has been connected" }
        return socket
    }

    override fun onBattleViewExit() {
        send(NotReadyAction(currentPlayer))
    }

    override fun onWindowClose() {
        leaveBattle()
    }

    override fun onDisconnect(socket: PlayerSocket) {
        val player = socket.player!!
        send(DisconnectAction(player))
        eventbus {
            +PlayerWasDisconnected(player)
        }

    }

    private fun nextTurn(): String {
        log { "previous turn: $turnPlayer" }
        var nextTurnIndex = turnList.indexOf(turnPlayer) + 1
        if (nextTurnIndex > turnList.size - 1) {
            nextTurnIndex = 0
        }
        turnPlayer = turnList[nextTurnIndex]
        log { "next turn: $turnPlayer" }
        return turnPlayer
    }

    private fun fleetsReadinessExcept(
        player: String,
        model: BattleModel,
    ): FleetsReadinessAction {
        val states = model.fleetsReadiness
            .exclude(player)
            .map { (player, state) ->
                player to state.map { (shipType, number) -> shipType to number.value }.toMap()
            }
            .toMap()

        return FleetsReadinessAction(states)
    }

}






