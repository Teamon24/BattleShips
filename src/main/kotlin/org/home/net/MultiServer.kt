package org.home.net

import org.home.utils.PlayersSockets
import org.home.utils.PlayersSocketsMessages
import org.home.utils.SocketUtils.receiveBatch
import org.home.utils.log
import java.io.InterruptedIOException
import java.net.ServerSocket
import kotlin.concurrent.thread



abstract class MultiServer<T: Message> {
    private val readTimeout = 100

    internal val socketsQueue = PlayersSockets()

    private lateinit var serverSocket: ServerSocket
    private val socketMessagesQueue = PlayersSocketsMessages<T>()

    private fun PlayerSocket.withTimeout(): PlayerSocket {
        soTimeout = readTimeout
        return this
    }

    abstract fun  process(socket: PlayerSocket, message: T)

    init {
        thread(name = "processor") {
            while (true) {
                val (socket, messages) = socketMessagesQueue.take()
                messages.forEach { process(socket, it) }
            }
        }

        thread(name = "receiver") {
            while (true) {
                socketsQueue.forEach { socket ->
                    try {
                        val receivedMessages = socket.receiveBatch<T>()
                        socketMessagesQueue.add(socket to receivedMessages)
                    } catch (e: InterruptedIOException) {
                        log { "trying to listen to next socket: socket has not sent any message" }
                    }
                }
            }
        }
    }

    fun start(port: Int) {
        serverSocket = ServerSocket(port)

        log { "server socket is created" }

        thread(name = "connection listener") {
            while (true) {
                val socket = PlayerSocket(serverSocket.accept())
                log { "client has been connected" }
                socketsQueue.add(socket.withTimeout())
            }
        }
    }
}

