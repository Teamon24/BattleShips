package org.home.net

import org.home.mvc.contoller.GameTypeController
import org.home.net.socket.ex.receiveSign
import org.home.net.socket.ex.sendSign
import java.io.InputStream
import java.io.OutputStream
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.threadPrintln

class BattleServer(private val gameController: GameTypeController) : MultiServer() {

    override suspend fun receive(`in`: InputStream, out: OutputStream) {
        while (true) {

            val message = receive(`in`)

            val response: Message = when (message) {
                is ShotMessage -> gameController.onShot(message)
                is HitMessage -> gameController.onHit(message)
                is EmptyMessage -> gameController.onEmpty(message)
                is DefeatMessage -> gameController.onDefeat(message)

                is DisconnectMessage -> {
                    gameController.onDisconnect(message)
                    break
                }

                is EndGameMessage -> {
                    gameController.onEndGame(message)
                    break
                }

                is MissMessage -> gameController.onMiss(message)
                is ConnectMessage -> gameController.onConnect(message)
                else -> gameController.onMessage(message)
            }

            out.send(response)
        }
    }

    override suspend fun send(msg: Message, out: OutputStream) {
        TODO("server sending logic is not implemented")
    }

    fun receive(`in`: InputStream): Message {
        val message = `in`.read<Message>()
        threadPrintln("$receiveSign \"$message\"")
        return message
    }

    fun OutputStream.send(msg: Message) {
        this.write(msg)
        threadPrintln("$sendSign \"$msg\"")
    }
}