package org.home.mvc.contoller.server

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.exclude
import home.extensions.CollectionsExtensions.isNotEmpty
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.BattleEvent
import org.home.mvc.contoller.events.BattleIsContinued
import org.home.mvc.contoller.events.BattleIsStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.FleetsReadinessReceived
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
import org.home.mvc.contoller.server.BattleClient.ActionTypeAbsentException
import org.home.mvc.contoller.server.PlayersSocketsExtensions.exclude
import org.home.mvc.contoller.server.PlayersSocketsExtensions.get
import org.home.mvc.contoller.server.PlayersSocketsExtensions.isNotClosed
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.contoller.server.action.AreReadyAction
import org.home.mvc.contoller.server.action.BattleContinuationAction
import org.home.mvc.contoller.server.action.BattleStartAction
import org.home.mvc.contoller.server.action.ConnectionAction
import org.home.mvc.contoller.server.action.ConnectionsAction
import org.home.mvc.contoller.server.action.DefeatAction
import org.home.mvc.contoller.server.action.DisconnectAction
import org.home.mvc.contoller.server.action.FleetEditAction
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.FleetsReadinessAction
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.LeaveAction
import org.home.mvc.contoller.server.action.MissAction
import org.home.mvc.contoller.server.action.NewServerAction
import org.home.mvc.contoller.server.action.NewServerConnectionAction
import org.home.mvc.contoller.server.action.NotReadyAction
import org.home.mvc.contoller.server.action.PlayerAction
import org.home.mvc.contoller.server.action.PlayerReadinessAction
import org.home.mvc.contoller.server.action.PlayerToRemoveAction
import org.home.mvc.contoller.server.action.ReadyAction
import org.home.mvc.contoller.server.action.ShipAdditionAction
import org.home.mvc.contoller.server.action.ShipDeletionAction
import org.home.mvc.contoller.server.action.ShotAction
import org.home.mvc.contoller.server.action.SinkingAction
import org.home.mvc.contoller.server.action.TurnAction
import org.home.mvc.model.BattleViewModel
import org.home.mvc.model.thoseAreReady
import org.home.mvc.view.battle.subscription.NewServerInfo
import org.home.net.server.Message
import org.home.net.server.MultiServer
import org.home.utils.DSLContainer
import org.home.utils.SocketUtils.send
import org.home.utils.log

class BattleServer : MultiServer<Action, PlayerSocket>(), BattleController<Action> {

    private val battleEndingComponent: BattleEndingComponent by gameScope()
    private val awaitConditions: AwaitConditions by gameScope()
    private val shotProcessingComponent by noScope<ShotProcessingComponent>()
    private val playerTurnComponent by noScope<PlayerTurnComponent>()

    override val currentPlayer = super.currentPlayer

    private fun socket(player: String): PlayerSocket = sockets[player]
    private fun BattleServer.excluding(player: String) = sockets.exclude(player)

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
        connector.interrupt()
        processor.interrupt()
        receiver.interrupt()
        serverSocket().close()
    }

    override fun leaveBattle() {
        send(LeaveAction(currentPlayer))
        modelView {
            if (battleIsEnded()) return@modelView

            if (battleIsStarted() && hasEnemies()) {
                playerTurnComponent {
                    remove(currentPlayer)
                    currentPlayer.hasATurn { nextTurn() }
                    send(NewServerAction(turnPlayer!!, turnList))
                }
                waitNewServerAndThenSend()
            }

            if (battleIsNotStarted() && hasEnemies()) {
                send(NewServerAction(exclude(currentPlayer).first()))
                waitNewServerAndThenSend()
            }
        }

        disconnect()
    }

    private fun waitNewServerAndThenSend() {
        awaitConditions.newServerFound.await()
        modelView {
            excluding(getNewServer().player).send(NewServerConnectionAction(getNewServer()))
        }
    }

    override fun endBattle() {
        battleEndingComponent.endBattle()
    }

    override fun connect(ip: String, port: Int) {
        start(port)
    }

    override fun accept(): PlayerSocket {
        val socket = PlayerSocket(serverSocket().accept())
        log { "client has been connected" }
        return socket
    }

    override fun onDisconnect(socket: PlayerSocket) {
        socket.isNotClosed {
            DisconnectAction(player!!).also {
                send(it)
                eventbus(PlayerWasDisconnected(it))
            }
        }
    }

    override fun continueBattle() {
        send(BattleContinuationAction)
        eventbus(BattleIsContinued)
    }

    override fun setTurn(newServerInfo: NewServerInfo) {
        newServerInfo {
            turnList.isNotEmpty {
                playerTurnComponent.turnList = turnList
                playerTurnComponent.turnPlayer = player
            }
        }
    }

    override fun process(socket: PlayerSocket, message: Action) {
        val action = message

        when (action) {
            is ConnectionAction -> onConnect(socket, action)
            is NotReadyAction -> processReadiness(action, ::PlayerIsNotReadyReceived)
            is ReadyAction -> processReadiness(action, ::PlayerIsReadyReceived)
            is FleetsReadinessAction -> {
                eventbus(FleetsReadinessReceived(action))
                action.states.keys.forEach { player ->
                    excluding(player).send(action)
                }
            }

            is ShipAdditionAction -> processFleetEdit(action, ::ShipWasAdded)
            is ShipDeletionAction -> processFleetEdit(action, ::ShipWasDeleted)

            is SinkingAction -> shotProcessingComponent.onSinking(action)
            is ShotAction -> shotProcessingComponent.onShot(action)
            is HitAction -> shotProcessingComponent.onHit(action)
            is MissAction -> shotProcessingComponent.onMiss(action)

            is PlayerToRemoveAction -> sendRemovePlayer(action)

            is NewServerConnectionAction -> awaitConditions.newServerFound.notifyUI() {
                action {
                    setNewServer(NewServerInfo(player, playerTurnComponent.turnList, ip, port))
                }
            }

            else -> throw ActionTypeAbsentException(action.javaClass.name, javaClass.name, "process")
        }
    }

    private fun onConnect(socket: PlayerSocket, action: ConnectionAction) {
        if (sockets.size + 1 < modelView.getPlayersNumber().value) {
            permitToConnect()
        }

        action.also {
            val connected = it.player
            socket.player = connected

            eventbus(ConnectedPlayerReceived(it))

            modelView {
                log { "hasNoServerTransfer: ${hasNoServerTransfer()}"}
                hasNoServerTransfer().so {
                    excluding(connected).send(it)
                    socket(connected).send {
                        +FleetSettingsAction(modelView)
                        +ConnectionsAction(exclude(connected))
                        +fleetsReadinessExcept(connected, modelView)
                        getReadyPlayers().isNotEmpty {
                            +AreReadyAction(thoseAreReady)
                        }
                    }
                }
            }
        }
    }


    private fun processFleetEdit(action: FleetEditAction, event: (FleetEditAction) -> FleetEditEvent) {
        action.let {
            excluding(it.player).send(it)
            eventbus(event(it))
        }
    }

    private fun processReadiness(action: PlayerReadinessAction, event: (PlayerReadinessAction) -> HasAPlayer) {
        action.let {
            excluding(it.player).send(it)
            eventbus(event(it))
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
                    playerTurnComponent {
                        remove(removedPlayer)
                        hasPlayers {
                            hasATurn(currentPlayer) {
                                +TurnReceived(TurnAction(currentPlayer))
                            } otherwise {
                                val shooter = action.shooter
                                socket(shooter).send(TurnAction(shooter))
                            }
                        }
                    }
                }
            }

            playerTurnComponent {
                battleIsStarted {
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

    private fun fleetsReadinessExcept(player: String, modelView: BattleViewModel): FleetsReadinessAction {
        val states = modelView.getFleetsReadiness()
            .exclude(player)
            .map { (player, state) ->
                player to state.map { (shipType, number) -> shipType to number.value }.toMap()
            }
            .toMap()

        return FleetsReadinessAction(states)
    }

    private fun DSLContainer<BattleEvent>.removeAndFire(
        toRemovedAction: PlayerAction,
        event: (PlayerAction) -> PlayerToRemoveReceived,
    ) {
        sockets.removeIf {
            it.player == toRemovedAction.player
        }

        + event(toRemovedAction)

        modelView.battleIsNotStarted().so {
            permitToConnect()
        }
    }
}






