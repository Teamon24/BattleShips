package org.home.net

import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.model.BattleModel
import org.home.utils.SocketUtils.receive
import org.home.utils.SocketUtils.sendAll
import org.home.utils.functions.exclude
import org.home.utils.logThreadClientWaiting
import java.net.Socket

class BattleServer : BattleController() {

    private val gameController: GameTypeController by di()
    private val model: BattleModel by di()

    private val multiServer = object : MultiServer() {

        override fun listen(client: Socket,
                            clients: MutableMap<Socket, String>) {

            clients[client] = ""
            val `in` = client.getInputStream()

            while (true) {
                logThreadClientWaiting(clients, client)
                val message = `in`.receive()
                process(message, client, clients)
            }
        }
    }

    private fun process(
        message: Message,
        client: Socket,
        clients: MutableMap<Socket, String>,
    ) {
        when (message) {
            is ShotMessage -> gameController.onShot(message)
            is HitMessage -> gameController.onHit(message)
            is EmptyMessage -> gameController.onEmpty(message)
            is DefeatMessage -> gameController.onDefeat(message)

            is DisconnectMessage -> gameController.onDisconnect(message)
            is EndGameMessage -> gameController.onEndGame(message)

            is MissMessage -> gameController.onMiss(message)
            is ConnectMessage -> gameController.onConnect(client, clients, message)
            is ReadyMessage -> {
                clients.exclude(client).sendAll(message)

                val turnMessage = gameController.onReady(message)

                turnMessage?.also { msg ->
                    clients.exclude(client).sendAll(msg)
                    gameController.onTurn(msg)
                }
            }

            else -> gameController.onMessage(message)
        }
    }

    fun start(port: Int) {
        multiServer.start(port)
    }

    fun listen(client: Socket, clients: MutableMap<Socket, String>) {
        multiServer.listen(client, clients)
    }

    override fun onFleetCreationViewExit() {
        TODO("Not yet implemented")
    }

    override fun startBattle() {
        val currentPlayer = applicationProperties.currentPlayer
        model.readyPlayers[currentPlayer]!!.value = true
        val readyMessage = ReadyMessage(currentPlayer)
        multiServer.clients.sendAll(readyMessage)
    }

    override fun hitLogic(hitMessage: HitMessage) {
        multiServer.clients.sendAll(hitMessage)
    }
}




