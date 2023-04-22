package org.home.net

import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.utils.PlayersSockets
import org.home.utils.SocketUtils.sendAll
import org.home.utils.extensions.exclude

class BattleServer : BattleController() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()

    private val multiServer = object : MultiServer<Action>() {
        override fun process(socket: PlayerSocket, action: Action) {
            when (action) {
                is ShotAction -> gameController.onShot(action)
                is HitAction -> gameController.onHit(action)
                is EmptyAction -> gameController.onEmpty(action)
                is DefeatAction -> gameController.onDefeat(action)

                is DisconnectAction -> gameController.onDisconnect(action)
                is EndGameAction -> gameController.onEndGame(action)

                is MissAction -> gameController.onMiss(action)
                is ConnectAction -> gameController.onConnect(socket, sockets, action)
                is ReadyAction -> {
                    sockets.exclude(socket).sendAll(action)

                    val turnMessage = gameController.onReady(action)

                    turnMessage?.also { msg ->
                        sockets.exclude(socket).sendAll(msg)
                        gameController.onTurn(msg)
                    }
                }

                else -> gameController.onMessage(action)
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
        val readyMessage = ReadyAction(currentPlayer)
        sockets.sendAll(readyMessage)
    }

    override fun hitLogic(hitMessage: HitAction) {
        sockets.sendAll(hitMessage)
    }
}




