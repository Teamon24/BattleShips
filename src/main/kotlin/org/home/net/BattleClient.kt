package org.home.net

import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.launch
import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.events.PlayerIsReadyAccepted
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.ReadyPlayersAccepted
import org.home.mvc.contoller.events.WaitForYourTurn
import org.home.mvc.model.BattleModel
import org.home.net.Condition.Companion.condition
import org.home.utils.MessageIO.write
import org.home.utils.SocketUtils.receiveBatch
import org.home.utils.extensions.singleThreadScope
import org.home.utils.log
import org.home.utils.logSend
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException

class BattleClient: BattleController() {

    companion object {
        var fleetSettingsReceived = condition("fleet settings received")
    }

    private val gameController: GameTypeController by di()
    private val appProps: ApplicationProperties by di()
    private val model: BattleModel by di()

    private lateinit var `in`: InputStream
    private lateinit var out: OutputStream
    private lateinit var socket: Socket
    private val receiver = singleThreadScope("receiver-#${appProps.currentPlayer}")

    @Throws(UnknownHostException::class, IOException::class)
    fun connect(ip: String, port: Int) {
        socket = Socket(ip, port, )
        out = socket.getOutputStream()
        `in` = socket.getInputStream()
        log { "connected to $ip:$port" }
    }

    @Throws(IOException::class)
    fun listen() {
        log { "client is listening for server..." }
        receiver.launch {
            while (true) {
                receiving()
            }
        }
    }

    fun send(action: Action) {
        out.write(action)
        logSend { action }
    }

    private fun receiving() {
        log { "waiting for message..." }
        val actions = socket.receiveBatch<Action>()
        actions.forEach(::process)
    }

    private fun process(action: Action) {
        when (action) {
            is ConnectAction -> fire(PlayerWasConnected(action.player))

            is FleetSettingsAction -> {
                fleetSettingsReceived.notifyUI { model.put(action) }
            }

            is ReadyPlayersAction -> {
                model.readyPlayers
                    .filter { action.players.contains(it.key) }
                    .forEach { (player, _) -> model.readyPlayers[player]!!.value = true }

                fire(ReadyPlayersAccepted(action.players))
            }
            is PlayersAction -> gameController.onPlayers(action)

            is ReadyAction -> {
                model.readyPlayers[action.player] = SimpleBooleanProperty(true)
                fire(PlayerIsReadyAccepted(action.player))
            }

            is TurnAction -> gameController.onTurn(action)
            else -> throw RuntimeException(
                "There is no case for class ${action::class.simpleName} in client#receive method"
            )
        }
    }

    private fun sendResponse(action: Action) {
        send(TextAction(action.actionType, "${super.applicationProperties.currentPlayer} received a message"))
    }

    fun close() {
        `in`.close()
        out.close()
        socket.close()
    }

    override fun onFleetCreationViewExit() {
        TODO("Not yet implemented")
    }

    override fun startBattle() {
        val readyPlayer = applicationProperties.currentPlayer
        model.readyPlayers[readyPlayer]!!.value = true
        fire(WaitForYourTurn)
        send(ReadyAction(readyPlayer))
    }

    override fun hitLogic(hitMessage: HitAction) {
        send(hitMessage)
        gameController.onHit(hitMessage)
    }


}
