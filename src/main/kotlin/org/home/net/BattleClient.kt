package org.home.net

import kotlinx.coroutines.launch
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.net.socket.ex.receiveSign
import org.home.net.socket.ex.sendSign
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.ioScope
import org.home.utils.threadPrintln
import tornadofx.Controller
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException

class BattleClient: BattleController() {
    private val gameController: GameTypeController by di()
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
        threadPrintln("client is listening for server")
        ioScope.launch {
            while (true) receive()
        }
    }

    fun send(msg: ActionMessage) {
        out.write(msg)
        threadPrintln("$sendSign \"$msg\"")
    }

    fun receive(): Message {
        threadPrintln("waiting for message")
        val response = `in`.read<Message>()
        threadPrintln("$receiveSign $response")

        when (response) {
            is HitMessage -> gameController.onHit(response)
            is ShotMessage -> gameController.onShot(response)
            is ConnectMessage -> gameController.onConnect(response)
            is DisconnectMessage -> gameController.onDisconnect(response)
            is DefeatMessage -> gameController.onDefeat(response)
            is EndGameMessage -> gameController.onEndGame(response)
            is TurnMessage -> gameController.onTurn(response)
            is MissMessage -> gameController.onMiss(response)
            is FleetSettingsMessage -> model.put(response)
            is PlayersMessage -> gameController.onPlayers(response)
            else -> throw RuntimeException(
                "There is no case for class ${response::class.simpleName} in client#receive method"
            )
        }

        return response
    }

    fun stop() {
        `in`.close()
        out.close()
        socket.close()
    }
}