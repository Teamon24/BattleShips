package org.home.net

import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.utils.SocketUtils.receive
import org.home.utils.SocketUtils.sendAll
import org.home.utils.functions.exclude
import java.net.Socket

class BattleServer : BattleController() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()

    private val multiServer = object : MultiServer() {
        override fun listen(socket: Socket) {
            val `in` = socket.getInputStream()
            while (true) {
                val message = `in`.receive()
                process(message, socket)
            }
        }
    }

    private val sockets = multiServer.sockets

    private fun process(message: Message, socket: Socket) {
        when (message) {
            is ShotMessage -> gameController.onShot(message)
            is HitMessage -> gameController.onHit(message)
            is EmptyMessage -> gameController.onEmpty(message)
            is DefeatMessage -> gameController.onDefeat(message)

            is DisconnectMessage -> gameController.onDisconnect(message)
            is EndGameMessage -> gameController.onEndGame(message)

            is MissMessage -> gameController.onMiss(message)
            is ConnectMessage -> {
                sockets[message.player] = socket
                gameController.onConnect(sockets, message)
            }
            is ReadyMessage -> {
                sockets.exclude(socket).sendAll(message)

                val turnMessage = gameController.onReady(message)

                turnMessage?.also { msg ->
                    sockets.exclude(socket).sendAll(msg)
                    gameController.onTurn(msg)
                }
            }

            else -> gameController.onMessage(message)
        }
    }

    fun start(port: Int) {
        multiServer.start(port)
    }

    fun listen(socket: Socket) {
        multiServer.listen(socket)
    }

    override fun onFleetCreationViewExit() {
        TODO("Not yet implemented")
    }

    override fun startBattle() {
        val currentPlayer = applicationProperties.currentPlayer
        model.readyPlayers[currentPlayer]!!.value = true
        val readyMessage = ReadyMessage(currentPlayer)
        sockets.sendAll(readyMessage)
    }

    override fun hitLogic(hitMessage: HitMessage) {
        sockets.sendAll(hitMessage)
    }
}




