package org.home.net

import javafx.beans.property.SimpleBooleanProperty
import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.events.PlayerIsReadyAccepted
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.ReadyPlayersAccepted
import org.home.mvc.contoller.events.WaitForYourTurn
import org.home.mvc.model.BattleModel
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.SocketUtils.receiveSign
import org.home.utils.SocketUtils.sendSign
import org.home.utils.log
import org.home.utils.functions.singleThreadScope
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException

class BattleClient: BattleController() {
    private val gameController: GameTypeController by di()
    private val appProps: ApplicationProperties by di()
    private val model: BattleModel by di()

    private lateinit var `in`: InputStream
    private lateinit var out: OutputStream
    private lateinit var socket: Socket

    @Throws(UnknownHostException::class, IOException::class)
    fun connect(ip: String, port: Int) {
        socket = Socket(ip, port, )
        out = socket.getOutputStream()
        `in` = socket.getInputStream()
        log { "connected to $ip:$port" }
    }

    @Throws(IOException::class)
    fun listen() {
        log { "client is listening for server" }
        singleThreadScope("receiver-#${appProps.currentPlayer}") {
            while (true) {
                receiving()
            }
        }
    }

    fun send(msg: Message) {
        out.write(msg)
        log { "$sendSign \"$msg\"" }
    }

    private fun receiving(): Message {
        log { "waiting for message" }
        val message = `in`.read<Message>()
        log { "$receiveSign $message" }

        when (message) {
            is ConnectMessage -> fire(PlayerWasConnected(message.player))

            is FleetSettingsMessage -> {
                model.put(message)
                model.commit()
            }

            is ReadyPlayersMessage -> {
                model.readyPlayers
                    .filter { message.players.contains(it.key) }
                    .forEach { (player, _) -> model.readyPlayers[player]!!.value = true }

                fire(ReadyPlayersAccepted(message.players))
            }
            is PlayersMessage -> gameController.onPlayers(message)

            is ReadyMessage -> {
                model.readyPlayers[message.player] = SimpleBooleanProperty(true)
                fire(PlayerIsReadyAccepted(message.player))
            }

            is TurnMessage -> gameController.onTurn(message)
            else -> throw RuntimeException(
                "There is no case for class ${message::class.simpleName} in client#receive method"
            )
        }

        return message
    }

    private fun sendResponse(message: Message) {
        send(TextMessage(message.actionType, "${super.applicationProperties.currentPlayer} received a message"))
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
        send(ReadyMessage(readyPlayer))
    }

    override fun hitLogic(hitMessage: HitMessage) {
        send(hitMessage)
        gameController.onHit(hitMessage)
    }
}