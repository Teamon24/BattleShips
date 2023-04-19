package org.home.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.mvc.contoller.GameTypeController
import org.home.utils.threadsScope
import org.home.utils.singleThreadScope

class ServerDemo {

    companion object {
        val gameController = object : GameTypeController() {
            override fun onHit(msg: HitMessage) = msg("onHit")
            override fun onShot(msg: ShotMessage) = msg("onShot")
            override fun onEmpty(msg: EmptyMessage) = msg("onEmpty")
            override fun onDefeat(msg: DefeatMessage) = msg("onDefeat")
            override fun onDisconnect(msg: DisconnectMessage) = msg("onDisconnect")
            override fun onEndGame(msg: EndGameMessage) = msg("onEndGame")
            override fun onMiss(msg: MissMessage) = msg("onMiss")
            override fun onConnect(msg: ConnectMessage) = msg("onConnect")
            override fun onMessage(msg: Message) { msg("UNIFIED") }
            override fun onTurn(msg: TurnMessage) { msg("UNIFIED") }
            override fun onFleetSettings(msg: FleetSettingsMessage) { msg("UNIFIED") }
            override fun onPlayers(msg: PlayersMessage) { msg("UNIFIED") }
        }

        fun msg(act: String) = HitMessage(act.rsp, 0, "player", "target")
        private val String.rsp get() = "$this response"
        private val battleServer = BattleServer()

        @JvmStatic
        fun main(args: Array<String>) {
            val port = 4444
            singleThreadScope("server") { battleServer.start(port) }

            val threads = 5
            val clientPoll = threadsScope(threads, "client-pool")

            val clients = 10
            val delayAfterConnection: Long = 5000
            val send = command(clients, clientPoll, port, delayAfterConnection) {
                this.send(it)
            }

            runBlocking {
                send.onEach { it.start() }
                    .forEach { it.join() }
            }

        }

        private fun command(
            threads: Int,
            clientPoll: CoroutineScope,
            port: Int,
            delayAfterConnection: Long,
            op: BattleClient.(ActionMessage) -> Unit
        ): List<Job> = (1..threads).map {
            val message = message(it)
            clientPoll.launch(start = CoroutineStart.LAZY) {
                val battleClient = BattleClient()
                battleClient.connect("localhost", port)
                delay(delayAfterConnection)
                battleClient.send(message)
            }
        }

        private fun message(it: Int): HitMessage {
            val message = HitMessage("A", it, "client-$it", "T$it")
            return message
        }
    }
}