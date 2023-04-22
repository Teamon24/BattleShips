package org.home.net

import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.utils.SocketUtils.sendAll
import org.home.utils.functions.exclude

class BattleServer : BattleController() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()

    private val multiServer = object : MultiServer<Message>() {
        override fun process(socket: PlayerSocket, message: Message) {
            when (message) {
                is ShotMessage -> gameController.onShot(message)
                is HitMessage -> gameController.onHit(message)
                is EmptyMessage -> gameController.onEmpty(message)
                is DefeatMessage -> gameController.onDefeat(message)

                is DisconnectMessage -> gameController.onDisconnect(message)
                is EndGameMessage -> gameController.onEndGame(message)

                is MissMessage -> gameController.onMiss(message)
                is ConnectMessage -> gameController.onConnect(socket, sockets, message)
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
    }


    private val sockets: PlayersSockets = multiServer.socketsQueue

    fun start(port: Int) {
        multiServer.start(port)
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




