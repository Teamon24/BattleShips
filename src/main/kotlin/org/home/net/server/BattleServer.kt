package org.home.net.server

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.invoke
import home.extensions.AtomicBooleansExtensions.invoke
import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.otherwise
import home.extensions.CollectionsExtensions.exclude
import org.home.app.AbstractApp.Companion.newGame
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
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
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.thoseAreReady
import org.home.net.BattleClient.ActionTypeAbsentException
import org.home.net.PlayerSocket
import org.home.net.isNotClosed
import org.home.net.message.Action
import org.home.net.message.AreReadyAction
import org.home.net.message.BattleStartAction
import org.home.net.message.DefeatAction
import org.home.net.message.DisconnectAction
import org.home.net.message.FleetSettingsAction
import org.home.net.message.FleetsReadinessAction
import org.home.net.message.HitAction
import org.home.net.message.LeaveAction
import org.home.net.message.Message
import org.home.net.message.MissAction
import org.home.net.message.NewServerAction
import org.home.net.message.NewServerConnectionAction
import org.home.net.message.NotReadyAction
import org.home.net.message.PlayerAction
import org.home.net.message.PlayerConnectionAction
import org.home.net.message.PlayerReadinessAction
import org.home.net.message.PlayerToRemoveAction
import org.home.net.message.PlayersConnectionsAction
import org.home.net.message.ReadyAction
import org.home.net.message.ShipAction
import org.home.net.message.ShipAdditionAction
import org.home.net.message.ShipDeletionAction
import org.home.net.message.ShotAction
import org.home.net.message.TurnAction
import org.home.utils.DSLContainer
import org.home.utils.PlayersSocketsExtensions.exclude
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send
import org.home.utils.log
import tornadofx.FXEvent
import kotlin.concurrent.thread

class BattleServer : MultiServer<Action, PlayerSocket>(), BattleController<Action> {

    private val battleEventEmitter: BattleEventEmitter by di()
    private val awaitConditions: AwaitConditions by newGame()
    private val shotProcessingComponent: ShotProcessingComponent by di()
    private val playerTurnComponent: PlayerTurnComponent by di()

    private val turnPlayer get() = playerTurnComponent.turnPlayer
    override val currentPlayer = super.currentPlayer

    private fun socket(player: String) = sockets[player]
    private fun excluding(player: String) = sockets.exclude(player)

    override fun send(message: Message) = sockets.send(message)
    override fun send(messages: Collection<Message>) = sockets.send(messages)

    override fun startBattle() {
        val turnPlayer = playerTurnComponent.startTurn()
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
                send(NewServerAction(turnPlayer))
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

    override fun endBattle() {
        battleEventEmitter.endBattle()
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

    override fun process(socket: PlayerSocket, message: Action) {
        val action = message

        when (action) {
            is PlayerConnectionAction -> onConnect(socket, action)
            is NotReadyAction -> processReadiness(action, ::PlayerIsNotReadyReceived)
            is ReadyAction -> processReadiness(action, ::PlayerIsReadyReceived)

            is ShipAdditionAction -> processFleetEdit(action, ::ShipWasAdded)
            is ShipDeletionAction -> processFleetEdit(action, ::ShipWasDeleted)

            is ShotAction -> shotProcessingComponent.onShot(action)
            is HitAction -> shotProcessingComponent.onHitShot(action)

            is MissAction -> shotProcessingComponent.onMiss(action)

            is PlayerToRemoveAction -> sendRemovePlayer(action)
            is NewServerConnectionAction -> awaitConditions.newServerFound.notifyUI() {
                newServer = action.ip to action.port
            }

            else -> throw ActionTypeAbsentException(action.javaClass.name, javaClass.name, "process")
        }
    }

    private fun onConnect(socket: PlayerSocket, action: PlayerConnectionAction){
        if (sockets.size + 1 < model.playersNumber.value) {
            permitToConnect()
        }

        action.also {
            val connected = it.player

            socket.player = connected

            eventbus {
                +ConnectedPlayerReceived(it)
            }

            sockets.exclude(connected).send(it)
            socket(connected).send {
                +FleetSettingsAction(model)
                +PlayersConnectionsAction(model.players.exclude(connected))
                +fleetsReadinessExcept(connected, model)
                +AreReadyAction(model.thoseAreReady)
            }
        }
    }


    private fun processFleetEdit(action: ShipAction, event: (ShipAction) -> FleetEditEvent) {
        action.let {
            excluding(it.player).send(it)
            eventbus {
                +event(it)
            }
        }
    }

    private fun processReadiness(action: PlayerReadinessAction, event: (PlayerReadinessAction) -> HasAPlayer) {
        action.let {
            excluding(it.player).send(it)
            model.setReady(it.player, it.isReady)
            eventbus { +event(it) }
        }
    }


    private fun sendRemovePlayer(action: PlayerToRemoveAction) {
        val removedPlayer = action.player
        sockets.exclude(removedPlayer).send(action)

        eventbus {
            when (action) {
                is DisconnectAction -> removeAndFire(action, ::PlayerWasDisconnected)
                is LeaveAction -> removeAndFire(action, ::PlayerLeaved)
                is DefeatAction -> {
                    +PlayerWasDefeated(action)
                    playerTurnComponent {
                        remove(removedPlayer)
                        hasPlayers {
                            hasATurn(currentPlayer) {
                                +TurnReceived(TurnAction(currentPlayer))
                            } otherwise {
                                action {
                                    socket(shooter).send(TurnAction(shooter))
                                }
                            }
                        }
                    }
                }
            }

            playerTurnComponent {
                hasPlayers {
                    hasATurn(removedPlayer) {
                        val nextTurn = nextTurnAndRemove(removedPlayer)
                        hasPlayers {
                            TurnAction(nextTurn).let {
                                +TurnReceived(it)
                                sockets.send(it)
                            }
                        }
                    }
                }
            }
            log { "<${action.className}> turn = ${playerTurnComponent.turnList}" }
        }
    }


    private fun DSLContainer<FXEvent>.removeAndFire(
        toRemovedAction: PlayerAction,
        event: (PlayerAction) -> PlayerToRemoveReceived,
    ) {
        sockets.removeIf {
            it.player == toRemovedAction.player
        }

        + event(toRemovedAction)

        model.battleIsNotStarted {
            permitToConnect()
        }
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






