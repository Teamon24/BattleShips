package org.home.net

import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.net.socket.ex.receiveSign
import org.home.net.socket.ex.sendSign
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.log
import org.home.utils.logCom
import org.home.utils.threadPrintln
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import kotlin.concurrent.thread

class BattleServer : BattleController() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()

    private val multiServer = object : MultiServer() {
        override fun listen(client: Socket, clients: MutableMap<Socket, String>) {

            val `in` = client.getInputStream()
            while (true) {
                log {
                    "waiting for ${
                        clients[client]!!
                            .ifEmpty { "connection" }
                            .apply { ifNoEmpty { Thread.currentThread().name = "$this listener" } }
                    } "
                }
                val message = `in`.receive()
                when (message) {
                    is ShotMessage -> gameController.onShot(message)
                    is HitMessage -> gameController.onHit(message)
                    is EmptyMessage -> gameController.onEmpty(message)
                    is DefeatMessage -> gameController.onDefeat(message)

                    is DisconnectMessage -> gameController.onDisconnect(message)
                    is EndGameMessage -> gameController.onEndGame(message)

                    is MissMessage -> gameController.onMiss(message)
                    is ConnectMessage -> {
                        val playerConnectedMessage = message

                        gameController.onConnect(playerConnectedMessage)

                        val connectedPlayer = playerConnectedMessage.player

                        clients[client] = connectedPlayer
                        val out = client.getOutputStream()

                        logCom(connectedPlayer) {
                            out.sendAndReceive(FleetSettingsMessage(model), `in`)
                        }

                        logCom(connectedPlayer) {
                            out.send(PlayersMessage(connectedPlayer, model.playersNames))
                        }

                        thread(name = "onConnection listener") {
                            clients
                                .filter { it.value != connectedPlayer }
                                .sendAll(playerConnectedMessage, `in`)
                        }
                    }

                    else -> gameController.onMessage(message)
                }
            }
        }
    }

    private fun Map<Socket, String>.sendAll(
        message: ConnectMessage,
        `in`: InputStream,
    ) {
        forEach {
            logCom(it.value) {
                it.key.getOutputStream().send(message)
            }
        }
    }

    fun listen(client: Socket, clients: MutableMap<Socket, String>) {
        multiServer.listen(client, clients)
    }

    fun start(port: Int) {
        multiServer.start(port)
    }

    fun OutputStream.sendAndReceive(msg: Message, `in`: InputStream) {
        send(msg)
        `in`.receive()
    }

    private fun InputStream.receive(): Message {
        val message = read<Message>()
        threadPrintln("$receiveSign \"$message\"")
        return message
    }

    private fun OutputStream.send(msg: Message) {
        write(msg)
        threadPrintln("$sendSign \"$msg\"")
    }

    private fun String.ifNoEmpty(function: String.() -> Unit) {
        if (this.isNotEmpty()) {
            this.function()
        }
    }
}
