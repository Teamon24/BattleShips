package org.home.net

import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.Conditions
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.events.BattleStarted
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerTurnToShootReceived
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.contoller.events.ShipWasConstructed
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.model.hadHit
import org.home.mvc.model.removeDestroyedDeck
import org.home.mvc.view.openMessageWindow
import org.home.net.action.Action
import org.home.net.action.ActionExtensions.defeat
import org.home.net.action.ActionExtensions.hit
import org.home.net.action.ActionType.HIT
import org.home.net.action.ActionType.SHOT
import org.home.net.action.ActionType.CONNECT
import org.home.net.action.ActionType.DISCONNECT
import org.home.net.action.ActionType.SHIP_CREATION
import org.home.net.action.ActionType.SHIP_DELETION
import org.home.net.action.ActionType.LEAVE_BATTLE
import org.home.net.action.ActionType.NEW_SERVER
import org.home.net.action.ActionType.NEW_SERVER_CONNECTION
import org.home.net.action.ActionType.DEFEAT
import org.home.net.action.ActionType.BATTLE_ENDED
import org.home.net.action.ActionType.READY
import org.home.net.action.ActionType.NOT_READY
import org.home.net.action.ActionType.TURN
import org.home.net.action.ActionType.PLAYERS
import org.home.net.action.ActionType.READY_PLAYERS
import org.home.net.action.ActionType.FLEETS_READINESS
import org.home.net.action.ActionType.MISS
import org.home.net.action.ActionType.BATTLE_STARTED
import org.home.net.action.ActionType.FLEET_SETTINGS
import org.home.net.action.AreReadyAction
import org.home.net.action.ConnectionAction
import org.home.net.action.DefeatAction
import org.home.net.action.HitAction
import org.home.net.action.MissAction
import org.home.net.action.LeaveAction
import org.home.net.action.ReadyAction
import org.home.net.action.DisconnectAction
import org.home.net.action.NewServerAction
import org.home.net.action.ShotAction
import org.home.net.action.TurnAction
import org.home.utils.SocketUtils.receive
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.log
import org.home.utils.singleThreadScope
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

class BattleClient: BattleController() {
    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()

    private val conditions: Conditions by di()

    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var serverSocket: Socket

    private val currentPlayer = applicationProperties.currentPlayer
    private val receiver = singleThreadScope(currentPlayer)
    private lateinit var receiverJob: Job

    class ActionTypeAbsentException(any: Any, source: String, method: String):
    RuntimeException("There is no when-branch for $any in $source#$method")

    @Throws(UnknownHostException::class, IOException::class)
    fun connect(ip: String, port: Int) {
        serverSocket = Socket(ip, port)
        output = serverSocket.getOutputStream()
        input = serverSocket.getInputStream()
        log { "connected to $ip:$port" }
    }

    override fun connectAndSend(ip: String, port: Int) {
        connect(ip, port)
        send(ConnectionAction(currentPlayer))
    }

    @Throws(IOException::class)
    fun listen() {
        log { "client is listening for server ..." }
        receiverJob = receiver.launch {
            log { "receiver is launched" }
            while (isActive) {
                log { "waiting for message ..." }
                try {
                    serverSocket.receive<Action>().forEach(::process)
                } catch (e: SocketException) {
                    log { e.message }
                }
            }
        }
        receiverJob.start()
    }

    private fun process(action: Action) {
        when (action.type) {
            CONNECT -> fire(ConnectedPlayerReceived(action.cast()))
            FLEET_SETTINGS -> {
                conditions
                    .fleetSettingsReceived
                    .notifyUI {
                        putSettings(action.cast())
                    }
            }

            READY_PLAYERS -> fire(ReadyPlayersReceived(action.cast<AreReadyAction>().players))

            PLAYERS -> gameController.onPlayers(action.cast())

            READY,
            NOT_READY -> gameController.onReady(action.cast())

            FLEETS_READINESS -> fire(FleetsReadinessReceived(action.cast()))
            SHIP_CREATION -> fire(ShipWasConstructed(action.cast()))
            SHIP_DELETION -> fire(ShipWasDeleted(action.cast()))
            BATTLE_STARTED -> fire(BattleStarted)
            BATTLE_ENDED -> fire(BattleIsEnded(action.cast()))
            TURN -> fire(PlayerTurnToShootReceived(action.cast<TurnAction>().player))

            SHOT -> {
                val shotAction = action.cast<ShotAction>()
                if (shotAction.target == currentPlayer) {
                    val ships = model.playersAndShips[currentPlayer]!!
                    ships.hadHit(shotAction.shot)
                        .yes { onHit(ships, shotAction) }
                        .no {
                            MissAction(shotAction).also {
                                serverSocket.send(it)
                                fire(ThereWasAMiss(it))
                            }
                        }
                }
            }

            HIT -> fire(ShipWasHit(action.cast()))
            MISS -> fire(ThereWasAMiss(action.cast()))

            DEFEAT -> fire(PlayerWasDefeated(action.cast<DefeatAction>().player))
            LEAVE_BATTLE -> fire(PlayerLeaved(action.cast<LeaveAction>().player))
            DISCONNECT -> fire(PlayerWasDisconnected(action.cast<DisconnectAction>().player))
            NEW_SERVER -> fire(NewServerReceived(action.cast<NewServerAction>().player))

            NEW_SERVER_CONNECTION -> fire(NewServerConnectionReceived(action.cast()))

            else -> throw ActionTypeAbsentException(action.type, javaClass.name, "process")
        }
    }

    override fun send(action: Action) = serverSocket.send(action)

    override fun onWindowClose() {
        serverSocket.isClosed.no {
            leaveBattle()
        }
    }


    override fun onFleetCreationViewExit() {
        leaveBattle()
    }

    override fun startBattle() {
        val readyPlayer = currentPlayer
        model.playersReadiness[readyPlayer] = true
        serverSocket.send(ReadyAction(readyPlayer))
    }

    override fun leaveBattle() {
        send(LeaveAction(currentPlayer))
        disconnect()
    }

    override fun endGame(winner: String) {
        openMessageWindow { "Победитель: $winner" }
    }

    override fun disconnect() {
        input.close()
        output.close()
        serverSocket.close()
        runBlocking {
            receiverJob.cancel()
            log { "${this.javaClass }#receiver is canceled" }
        }
    }

    private fun onHit(ships: MutableList<Ship>, shotAction: ShotAction) {
        ships.removeDestroyedDeck(shotAction.shot)
        val hitAction = HitAction(shotAction)

        serverSocket.send {
            hit(hitAction)
            ships.ifEmpty { defeat(currentPlayer) }
        }

        fire(ShipWasHit(hitAction))

        ships.ifEmpty {
            fire(PlayerWasDefeated(currentPlayer))
        }
    }

    private inline fun <reified R: Action> Action.cast(): R = this as R
}
