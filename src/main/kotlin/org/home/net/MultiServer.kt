package org.home.net

import org.home.utils.SocketUtils.receiveAll
import org.home.utils.log
import java.io.InterruptedIOException
import java.io.Serializable
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

typealias PlayersSockets = LinkedBlockingQueue<PlayerSocket>
typealias PlayerSocketMessages<T> = Pair<PlayerSocket, Collection<T>>
typealias PlayersSocketsMessages<T> = LinkedBlockingQueue<PlayerSocketMessages<T>>

abstract class MultiServer<T: Serializable> {
    private val readTimeout = 1000

    private lateinit var serverSocket: ServerSocket

    private val socketMessagesQueue: PlayersSocketsMessages<T> = PlayersSocketsMessages()
    internal val socketsQueue: PlayersSockets = PlayersSockets()

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
                        val receivedMessages = socket.receiveAll<T>()
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

