package org.home.net.socket.ex.timeout

import kotlinx.coroutines.launch
import org.home.net.Message
import org.home.utils.SocketUtils.receiveAll
import org.home.utils.log
import org.home.utils.functions.singleThreadScope
import java.io.InterruptedIOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class CheckTimeoutServer {

    private val readTimeout = 1000

    private lateinit var serverSocket: ServerSocket

    private val messagesQueue: LinkedBlockingQueue<Message> = LinkedBlockingQueue()
    private val socketsQueue: LinkedBlockingQueue<Socket> = LinkedBlockingQueue()
    private val receiver = singleThreadScope("receiver")

    private val sockets: MutableMap<Socket, String> = mutableMapOf()
    private val thisServer = this@CheckTimeoutServer



    private fun Socket.withTimeout(): Socket {
        soTimeout = readTimeout
        return this
    }

    init {
        thread(name = "processor") {
            while (true) {
                val message = messagesQueue.take()
                log { message }
            }
        }

        receiver.launch {
            while (true) {
                while (thisServer.socketsQueue.size > 0) {
                    log { "taking socket form queue [size = ${thisServer.socketsQueue.size}]" }
                    val take = thisServer.socketsQueue.take()
                    sockets[take] = ""
                    log { "socket was added" }
                }

                sockets.forEach { (socket, player) ->
                    try {
                        log { "receiving" }
                        val receivedMessage = socket.getInputStream().receiveAll()
                        messagesQueue.addAll(receivedMessage)
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
                val socket = serverSocket.accept()
                log { "client has been connected" }
                socketsQueue.add(socket.withTimeout())
            }
        }
    }


}




