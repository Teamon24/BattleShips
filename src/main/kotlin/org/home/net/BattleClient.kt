package org.home.net

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.net.socket.ex.receiveSign
import org.home.net.socket.ex.sendSign
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.ioScope
import org.home.utils.log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException

class BattleClient: BattleController() {
    private val gameController: GameTypeController by di()
    private val applicationProperties: ApplicationProperties by di()
    private val model: BattleModel by di()

    private lateinit var `in`: InputStream
    private lateinit var out: OutputStream
    private lateinit var socket: Socket

    @Throws(UnknownHostException::class, IOException::class)
    fun connect(ip: String, port: Int) {
        socket = Socket(ip, port, )
        out = socket.getOutputStream()
        `in` = socket.getInputStream()
    }

    @Throws(IOException::class)
    fun listen() {
        log { "client is listening for server" }
        ioScope.launch {
            while (true) {
                receive()
            }
        }
    }

    fun send(msg: ActionMessage) {
        out.write(msg)
        log { "$sendSign \"$msg\"" }
    }

    fun receive(): Message {
        log { "waiting for message" }
        val message = `in`.read<Message>()
        log { "$receiveSign $message" }

        when (message) {
            is ConnectMessage -> runBlocking { gameController.onConnect(message) }
            is FleetSettingsMessage -> {
                model.put(message)
                send(TextMessage(message.actionType, "${applicationProperties.currentPlayer} received a message"))
            }
            is PlayersMessage -> gameController.onPlayers(message)
            else -> throw RuntimeException(
                "There is no case for class ${message::class.simpleName} in client#receive method"
            )
        }

        return message
    }

    fun stop() {
        `in`.close()
        out.close()
        socket.close()
    }
}