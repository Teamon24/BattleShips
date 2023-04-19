package org.home.net.socket.ex

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.net.BattleClient
import org.home.net.Message
import org.home.net.MultiServer
import org.home.utils.threadsScope
import org.home.utils.singleThreadScope
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

const val receiveSign = "<-"
const val sendSign = "->"

fun main() {
    val host = "127.0.0.1"
    val port = 5555

    val server = echoMultiServer()

    singleThreadScope("server").launch {
        server.start(port)
    }

    Thread.sleep(100)

    val threads = 7
    val threadPoolScope = threadsScope(threads, "client-pool")

    (1..threads).map {
        threadPoolScope.launch { GreetClient().send(it, host, port) }
    }.apply {
        runBlocking {
            onEach { it.start() }
            forEach { it.join() }
        }
    }

    server.stop()
}

private fun echoMultiServer() = object : MultiServer() {
    override suspend fun listen(`in`: InputStream, out: OutputStream) { EchoServer().handle(`in`, out) }
}

private fun GreetClient.send(number: Int, host: String, port: Int) {
    connect(host, port)
    sendAndReceive("hello, im client #$number")
    sendAndReceive(".")
    stop()
}