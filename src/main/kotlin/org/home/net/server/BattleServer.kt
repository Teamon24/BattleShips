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
import org.home.mvc.model.areHit
import org.home.mvc.model.removeDestroyedDeck
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
import org.home.utils.extensions.BooleansExtensions.invoke
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.extensions.CollectionsExtensions.hasElements
import org.home.utils.extensions.CollectionsExtensions.isNotEmpty
import org.home.utils.extensions.CollectionsExtensions.isEmpty
import org.home.utils.extensions.className
import org.home.utils.log
import tornadofx.FXEvent
import kotlin.concurrent.thread

class BattleServer : MultiServer<Action, PlayerSocket>() {
    private val notifierStrategies: ShotNotifierStrategies by di()
    private val awaitConditions: AwaitConditions by newGame()

    private inline val <E> Collection<E>.hasPlayers get() = hasElements
    private inline val String.hasTurn get() = this == turnPlayer

    private val shotNotifier = notifierStrategies.create(sockets)
    private val turnList: MutableList<String> = mutableListOf()

    private lateinit var turnPlayer: String

    override fun send(action: Action) = sockets.send(action)
    override fun send(actions: Collection<Action>) = sockets.send(actions)

    private fun String.socket() = sockets[this]

    private fun send(actions: MutableMap<String, MutableList<Action>>) = sockets.send(actions)
    private fun excluding(player: String) = sockets.exclude(player)

    override fun process(socket: PlayerSocket, message: Action) {
        val action = message

        when (action) {
            is PlayerConnectionAction -> {
                if (sockets.size + 1 < model.playersNumber.value) {
                    permitToConnect()
                }

                action.also {
                    val connected = it.player

                    socket.player = connected

                    eventbus {
                        +ConnectedPlayerReceived(it)
                    }

                    excluding(connected).send(it)

                    connected.socket().send {
                        +FleetSettingsAction(model)
                        +PlayersConnectionsAction(model.players.exclude(connected))
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
                if (target == currentPlayer) {
                    val ships = model.shipsOf(target)
                    val shot = action.shot
                    ships.areHit(shot).yes {
                        processHit(action.hit())
                        ships.removeDestroyedDeck(shot)
                        ships.isEmpty {
                            turnList.remove(target)
                            val shooter = action.player
                            val defeatAction = DefeatAction(shooter, target)

                            send { +defeatAction }


                            turnList.hasPlayers.so {
                                shooter.socket().send(TurnAction(shooter))
                            }
                            eventbus { +PlayerWasDefeated(defeatAction) }
                        }
                    } no {
                        onMiss(action)
                    }
                } else {
                    sockets.send(shotNotifier.notifications(action))
                    target.socket().send(action)
                }
            }

            is HitAction -> processHit(action)

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


    private fun processHit(hitAction: HitAction) {
        val notifications = shotNotifier.notifications(hitAction)
        send(notifications)
        eventbus {
            +ShipWasHit(hitAction)
        }
    }

    private fun processFleetEdit(action: ShipAction, event: (ShipAction) -> FleetEditEvent) {
        action.also {
            excluding(it.player).send(it)
            eventbus {
                +event(it)
            }
        }
    }

    private fun sendRemovePlayer(action: PlayerToRemoveAction) {
        val removedPlayer = action.player
        excluding(removedPlayer).send(action)

        eventbus {
            when (action) {
                is DisconnectAction -> removeAndFire(action, ::PlayerWasDisconnected)
                is LeaveAction -> removeAndFire(action, ::PlayerLeaved)
                is DefeatAction -> {
                    +PlayerWasDefeated(action)
                    turnList.remove(removedPlayer)
                    turnList.hasPlayers {
                        currentPlayer.hasTurn
                            .yes { +TurnReceived(TurnAction(currentPlayer)) }
                            .no { sockets[action.shooter].send(TurnAction(action.shooter)) }
                    }
                }
            }

            turnList.isNotEmpty {
                removedPlayer.hasTurn {
                    val nextTurn = nextTurn()
                    turnList.remove(removedPlayer)
                    turnList.hasPlayers {
                        TurnAction(nextTurn).let {
                            +TurnReceived(it)
                            send(it)
                        }
                    }
                }
            }
            log { "<${action.className}> turn = $turnList" }
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

        model.battleIsNotStarted {
            permitToConnect()
        }
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
        val playersAndShotMessages = shotNotifier.notifications(action)

        val nextTurn = nextTurn()
        val playersAndTurnAction = sockets.associate {
            it.player!! to mutableListOf(TurnAction(nextTurn))
        }

        sockets.send(playersAndShotMessages, playersAndTurnAction)

        return nextTurn
    }

    private fun processReadiness(action: PlayerReadinessAction, event: (PlayerReadinessAction) -> HasAPlayer) {
        action.let {
            excluding(it.player).send(it)
            model.setReady(it.player, it.isReady)
            eventbus { +event(it) }
        }
    }

    override fun startBattle() {
        turnList.addAll(model.players.shuffled())
        log { "turn $turnList" }
        turnPlayer = turnList.first()

        send {
            +BattleStartAction
            +TurnAction(turnPlayer)
        }

        eventbus {
            +BattleIsStarted
            +TurnReceived(TurnAction(turnPlayer))
        }
    }


    override fun disconnect() {
        sockets.forEach { it.close() }
        log { "interrupting ${connector.name}" }
        connector.interrupt()

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
            if (battleIsStarted && players.size > 1) {
                TODO("${this@BattleServer.className}#leaveBattle has no server transfer logic that tested for correct implementation")
                sockets.send(NewServerAction(turnPlayer))
                awaitConditions.newServerFound.await()
                excluding(turnPlayer).send(
                    NewServerConnectionAction(
                        turnPlayer,
                        newServer.first,
                        newServer.second))
            }
        }
        disconnect()
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
        connector.canProceed(false)
        leaveBattle()
    }

    override fun onWindowClose() {
        connector.canProceed(false)
        leaveBattle()
    }

    override fun onDisconnect(socket: PlayerSocket) {
        socket {
            isNotClosed {
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






