package org.home.net

import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.net.socket.ex.receiveSign
import org.home.net.socket.ex.sendSign
import java.io.InputStream
import java.io.OutputStream
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.threadPrintln

class BattleServer: BattleController() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()

    private val multiServer = object: MultiServer() {
        override suspend fun listen(`in`: InputStream, out: OutputStream) {
            while (true) {
                val message = receive(`in`)

                when (message) {
                    is ShotMessage -> gameController.onShot(message)
                    is HitMessage -> gameController.onHit(message)
                    is EmptyMessage -> gameController.onEmpty(message)
                    is DefeatMessage -> gameController.onDefeat(message)

                    is DisconnectMessage -> {
                        gameController.onDisconnect(message)
                        clients.forEach {
                            it.getOutputStream().send(message)
                        }
                    }

                    is EndGameMessage -> {
                        gameController.onEndGame(message)
                        clients.forEach {
                            it.getOutputStream().send(message)
                        }
                        break
                    }

                    is MissMessage -> gameController.onMiss(message)
                    is ConnectMessage -> {
                        gameController.onConnect(message)
                        out.send(FleetSettingsMessage(model))
                        out.send(PlayersMessage(model.playersNames))
                        clients.forEach { it.getOutputStream().send(message) }
                    }
                    else -> gameController.onMessage(message)
                }
            }
        }
    }


    suspend fun listen(`in`: InputStream, out: OutputStream) {
        multiServer.listen(`in`, out)
    }

    fun start(port: Int) {
        multiServer.start(port)
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