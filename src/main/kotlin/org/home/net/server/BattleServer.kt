package org.home.net.server

import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleIsStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerToRemoveReceived
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.net.BattleClient.ActionTypeAbsentException
import org.home.net.PlayerSocket
import org.home.net.isNotClosed
import org.home.net.message.Action
import org.home.net.message.AreReadyAction
import org.home.net.message.BattleEndAction
import org.home.net.message.BattleStartAction
import org.home.net.message.PlayersConnectionsAction
import org.home.net.message.PlayerConnectionAction
import org.home.net.message.DefeatAction
import org.home.net.message.DisconnectAction
import org.home.net.message.FleetSettingsAction
import org.home.net.message.FleetsReadinessAction
import org.home.net.message.HasAShot
import org.home.net.message.HitAction
import org.home.net.message.LeaveAction
import org.home.net.message.MissAction
import org.home.net.message.NewServerAction
import org.home.net.message.NewServerConnectionAction
import org.home.net.message.NotReadyAction
import org.home.net.message.PlayerAction
import org.home.net.message.PlayerReadinessAction
import org.home.net.message.PlayerToRemoveAction
import org.home.net.message.ReadyAction
import org.home.net.message.ShipAction
import org.home.net.message.ShipAdditionAction
import org.home.net.message.ShipDeletionAction
import org.home.net.message.ShotAction
import org.home.net.message.TurnAction
import org.home.utils.DSLContainer
import org.home.utils.PlayerSocketUtils.send
import org.home.utils.PlayersSocketsExtensions.exclude
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.extensions.CollectionsExtensions.hasElements
import org.home.utils.extensions.CollectionsExtensions.shuffledKeys
import org.home.utils.extensions.className
import org.home.utils.log
import tornadofx.FXEvent
import kotlin.concurrent.thread

class BattleServer: MultiServer<Action, PlayerSocket>() {
    private val notifierStrategies: ShotNotifierStrategies by di()
    private val awaitConditions: AwaitConditions by newGame()

    private val <E> Collection<E>.hasPlayers get() = hasElements
    private val String.hasTurn get() = this == turnPlayer

    private val shotNotifier = notifierStrategies.create(sockets)
    private val turnList: MutableList<String> = mutableListOf()

    private lateinit var turnPlayer: String

    override fun send(action: Action) = sockets.send(action)
    override fun send(actions: Collection<Action>) = sockets.send(actions)

    override fun process(socket: PlayerSocket, message: Action) {
        val action = message
        when (action) {
            is PlayerConnectionAction -> {
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
                        +PlayersConnectionsAction(model.playersNames.exclude(connected))
                        +fleetsReadinessExcept(connected, model)
                        +AreReadyAction(model.thoseAreReady)
                    }
                }
            }

            is NotReadyAction -> processReadiness(action, ::PlayerIsNotReadyReceived)
            is ReadyAction -> processReadiness(action, ::PlayerIsReadyReceived)
            is ShipAdditionAction -> processFleetEdit(action, ::ShipWasAdded)
            is ShipDeletionAction -> processFleetEdit(action, ::ShipWasDeleted)

            is ShotAction -> {
                val target = action.target
                val serverIsTarget = target == currentPlayer
                if (serverIsTarget) {
                    val shot = action.shot
                    model.registersAHit(shot)
                        .yes {
                            onHit(action)
                            val ships = model.shipsOf(currentPlayer)
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
                    +TurnReceived(TurnAction(nextTurn))
                }
            }

            is PlayerToRemoveAction -> sendRemovePlayer(action)
            is NewServerConnectionAction -> awaitConditions.newServerFound.notifyUI() {
                newServer = action.ip to action.port
            }

            else -> throw ActionTypeAbsentException(action.javaClass.name, javaClass.name, "process")
        }
    }

    private fun processFleetEdit(action: ShipAction, event: (ShipAction) -> FleetEditEvent) {
        sockets.exclude(action.player).send(action)
        eventbus {
            +event(action)
        }
    }

    private fun sendRemovePlayer(action: PlayerToRemoveAction) {
        val removedPlayer = action.player

        sockets
            .exclude(removedPlayer)
            .send(action)

        eventbus {
            when (action) {
                is DisconnectAction -> removeAndFire(action, ::PlayerWasDisconnected)
                is LeaveAction -> removeAndFire(action, ::PlayerLeaved)
                is DefeatAction -> {
                    +PlayerWasDefeated(action)
                    turnList.remove(removedPlayer)
                    currentPlayer.hasTurn yes {
                        turnList.hasPlayers so { +TurnReceived(TurnAction(currentPlayer)) }
                    } no {
                        sockets[action.shooter].send(TurnAction(action.shooter))
                    }
                }
            }

            turnList.isNotEmpty().so {
                log { "<${action.className}> turn = $turnList" }

                removedPlayer.hasTurn.so {
                    val nextTurn = nextTurn()
                    turnList.remove(removedPlayer)
                    turnList.hasPlayers.so {
                        TurnAction(nextTurn).also {
                            +TurnReceived(it)
                            send(it)
                        }
                    }
                }
            }
        }
    }

    private fun DSLContainer<FXEvent>.removeAndFire(
        toRemovedAction: PlayerAction,
        event: (PlayerAction) -> PlayerToRemoveReceived,
    ) {
        sockets.removeIf {
            it.player == toRemovedAction.player
        }

        +event(toRemovedAction)
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
            +TurnReceived(TurnAction(nextTurn))
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

    private fun processReadiness(action: PlayerReadinessAction, event: (PlayerReadinessAction) -> HasAPlayer) {
        sockets.exclude(action.player).send(action)
        action {
            model.playersReadiness[player] = isReady
            eventbus { +event(this@action) }
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
            +BattleIsStarted
            +TurnReceived(TurnAction(turnPlayer))
        }
    }


    override fun disconnect() {
        sockets.forEach { it.close() }
        log { "interrupting ${accepter.name}" }
        accepter.interrupt()

        log { "interrupting ${processor.name}" }
        processor.interrupt()

        log { "interrupting ${receiver.name}" }
        thread {
            while (receiver.canProceed()) {
                receiver.interrupt()
            }
        }
        serverSocket().close()
    }

    override fun leaveBattle() {
        send(LeaveAction(currentPlayer))
        log { "server is leaving battle" }
        model {
            log { "battleIsEnded = $battleIsEnded" }
            log { "battleIsStarted = $battleIsStarted" }
            if ((!battleIsEnded || battleIsStarted) && playersNames.size > 1) {
                TODO("${this@BattleServer.className}#leaveBattle has no server transfer logic that tested for correct implementation")
                sockets.send(NewServerAction(turnPlayer))
                awaitConditions.newServerFound.await()
                sockets.exclude(turnPlayer).send(
                    NewServerConnectionAction(
                        turnPlayer,
                        newServer.first,
                        newServer.second))
            }
        }
        disconnect()
    }

    override fun endBattle() {
        eventbus {
            +BattleIsEnded(BattleEndAction(model.getWinner()))
        }
    }

    override fun connect(ip: String, port: Int) {
        start(port)
    }

    override fun accept(): PlayerSocket {
        val socket = PlayerSocket(serverSocket().accept())
        log { "client has been connected" }
        return socket
    }

    override fun onBattleViewExit() {
        accepter.canProceed(false)
        leaveBattle()
    }

    override fun onWindowClose() {
        accepter.canProceed(false)
        leaveBattle()
    }

    override fun onDisconnect(socket: PlayerSocket) {
        socket {
            isNotClosed.so {
                DisconnectAction(player!!).also {
                    send(it)
                    eventbus {
                        +PlayerWasDisconnected(it)
                    }
                }
            }
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

    private fun fleetsReadinessExcept(player: String, model: BattleModel): FleetsReadinessAction {
        val states = model.fleetsReadiness
            .exclude(player)
            .map { (player, state) ->
                player to state.map { (shipType, number) -> shipType to number.value }.toMap()
            }
            .toMap()

        return FleetsReadinessAction(states)
    }
}






