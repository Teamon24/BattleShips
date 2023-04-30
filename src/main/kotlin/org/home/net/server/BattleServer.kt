package org.home.net.server

import org.home.mvc.contoller.Conditions
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleStarted
import org.home.mvc.contoller.events.DSLContainer
import org.home.mvc.contoller.events.DSLContainer.Companion.eventbus
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerToRemoveReceived
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.contoller.events.ShipWasConstructed
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.model.hadHit
import org.home.mvc.model.removeDestroyedDeck
import org.home.net.BattleClient.ActionTypeAbsentException
import org.home.net.PlayerSocket
import org.home.net.action.Action
import org.home.net.action.BattleEndAction
import org.home.net.action.BattleStartAction
import org.home.net.action.ConnectionAction
import org.home.net.action.DefeatAction
import org.home.net.action.DisconnectAction
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
import org.home.utils.PlayerSocketUtils.send
import org.home.utils.PlayersSocketsExtensions.exclude
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.CollectionsExtensions.shuffledKeys
import org.home.utils.extensions.className
import org.home.utils.extensions.ln
import org.home.utils.log
import org.home.utils.logging
import tornadofx.FXEvent

class BattleServer : MultiServer<Action, PlayerSocket>() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()
    private val notifierStrategies: ShotNotifierStrategies by di()
    private val shotNotifier = notifierStrategies.create(sockets)
    private val conditions: Conditions by di()

    private var turnList: MutableList<String>? = null
    private lateinit var turnPlayer: String

    override fun process(socket: PlayerSocket, message: Action) {
        val action = message
        when (action) {
            is ConnectionAction -> {
                permitToAccept(sockets.size + 1 < model.playersNumber.value)
                socket.player = action.player
                gameController.onConnect(sockets, action)
            }

            is ReadyAction,
            is NotReadyAction,
            -> processReadiness(action as PlayerReadinessAction)

            is ShipConstructionAction -> {
                sockets.exclude(action.player).send(action)
                fire(ShipWasConstructed(action))
            }

            is ShipDeletionAction -> {
                sockets.exclude(action.player).send(action)
                fire(ShipWasDeleted(action))
            }

            is ShotAction -> {
                val target = action.target

                val serverIsTarget = target == currentPlayer
                if (serverIsTarget) {
                    val shot = action.shot
                    val ships = model.playersAndShips[currentPlayer]!!
                    ships.hadHit(shot)
                        .yes { onHit(ships, action); return }
                        .no { onMiss(action) }
                } else {
                    sockets.send(shotNotifier.createNotifications(action))
                    sockets[action.target].send(action)
                }
            }

            is HitAction -> {
                sockets.send(shotNotifier.createNotifications(action))
                fire(ShipWasHit(action))
            }

            is MissAction -> {
                val nextTurn = notifyAboutShotAndSendNextTurn(action)
                fire(ThereWasAMiss(action))
                fire(TurnReceived(nextTurn))
            }

            is PlayerToRemoveAction -> sendRemovePlayer(action)
            is NewServerConnectionAction -> conditions.newServerFound.notifyUI() {
                newServer = action.ip to action.port
            }

            else -> throw ActionTypeAbsentException(action.javaClass.name, javaClass.name, "process")
        }
    }

    private fun sendRemovePlayer(action: PlayerToRemoveAction) {
        action {
            if (turnList != null) {
                turnList!!.remove(player)
            }

            eventbus {
                when (action) {
                    is DisconnectAction -> removeAndFire(action, ::PlayerWasDisconnected)
                    is LeaveAction -> removeAndFire(action, ::PlayerLeaved)
                    is DefeatAction -> fire(PlayerWasDefeated(player))
                }

                sockets.send {
                    + action
                    if (action.player == turnPlayer) {
                        turnPlayer = nextTurn()
                        turnList!!.remove(action.player)
                        + TurnAction(turnPlayer)
                        + TurnReceived(turnPlayer)
                    }
                }
            }
        }
    }

    private inline fun DSLContainer<FXEvent>.removeAndFire(
        playerToRemoveAction: PlayerToRemoveAction,
        event: (String) -> PlayerToRemoveReceived,
    ) {

        playerToRemoveAction {
            sockets.removeIf { socket -> socket.player == player }
            + event(player)
        }
    }

    private fun onHit(ships: MutableList<Ship>, shotAction: ShotAction) {
        val shot = shotAction.shot
        ships.removeDestroyedDeck(shot)
        val hitAction = HitAction(shotAction)

        sockets.send {
            + hitAction
            ships.isEmpty().so { + DefeatAction(currentPlayer) }
        }

        eventbus {
            + ShipWasHit(hitAction)
            ships.isEmpty().so { + PlayerWasDefeated(currentPlayer) }
        }


    }

    private fun onMiss(action: ShotAction) {
        val missAction = MissAction(action)
        val nextTurn = nextTurn()

        sockets.send {
            +missAction
            +TurnAction(nextTurn)
        }

        eventbus {
            +ThereWasAMiss(missAction)
            +TurnReceived(nextTurn)
        }
    }


    private fun notifyAboutShotAndSendNextTurn(action: HasAShot): String {
        val playersAndShotMessages = shotNotifier.createNotifications(action)

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

        turnList = shuffled
        log { "turn: $shuffled" }
        turnPlayer = player
        log { "first turn: $shuffled" }

        sockets.send {
            + ReadyAction(currentPlayer)
            + BattleStartAction
            + TurnAction(player)
        }

        eventbus {
            + BattleStarted
            + TurnReceived(turnPlayer)
        }
    }

    override fun send(action: Action) = sockets.send(action)

    override fun leaveBattle() {
        send(LeaveAction(currentPlayer))

        if (!model.battleIsEnded && model.playersNames.size != 1) {
            TODO("${this.className}#leaveBattle has no server transfer logic that tested for correct implementation")
            sockets.send(NewServerAction(turnPlayer))
            conditions.newServerFound.await()
            sockets.exclude(turnPlayer).send(
                NewServerConnectionAction(
                    turnPlayer,
                    model.newServer.first,
                    model.newServer.second))
        }

        sockets.forEach { it.close() }

        processor.interrupt()
        receiver.interrupt()
        accepter.interrupt()
    }

    override fun endGame(winner: String) {
        val battleEndAction = BattleEndAction(winner)
        sockets.send(battleEndAction)
        fire(BattleIsEnded(battleEndAction))
    }

    override fun disconnect() {
        throw UnsupportedOperationException("${this.className}#disconnect")
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

    override fun onWindowClose() {
        leaveBattle()
    }

    override fun onDisconnect(socket: PlayerSocket) {
        val player = socket.player!!
        sockets.send(DisconnectAction(player))
        fire(PlayerWasDisconnected(player))
    }

    fun nextTurn(): String {
        log { "previous turn: $turnPlayer" }
        var nextTurnIndex = turnList!!.indexOf(turnPlayer) + 1
        if (nextTurnIndex > turnList!!.size - 1) {
            nextTurnIndex = 0
        }
        turnPlayer = turnList!![nextTurnIndex]
        log { "next turn: $turnPlayer" }
        return turnPlayer
    }
}






