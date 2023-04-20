package org.home.net

import kotlinx.coroutines.launch
import org.home.mvc.view.openErrorWindow
import org.home.net.TimeoutInterruption.Companion.interruptAfter
import org.home.utils.SocketUtils.send
import org.home.utils.log
import org.home.utils.threadsScope
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.concurrent.thread


abstract class MultiServer {
    private lateinit var serverSocket: ServerSocket
    internal val clients: MutableMap<Socket, String> = mutableMapOf()

    abstract fun listen(client: Socket, clients: MutableMap<Socket, String>)

    private val threadsPool = threadsScope(4, name = "client listener")

    @Throws(IOException::class)
    fun start(port: Int) {
        serverSocket = ServerSocket(port)

        log { "server socket is created" }
        thread(name = "connection listener") {
            while (true) {
                val client = serverSocket.accept()
                log { "client has been connected" }

                clients[client] = ""

                threadsPool.launch {
                    listen(client, clients)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun start() {
        start(getFreePort())
    }

    fun getFreePort(): Int {
        try {
            return ServerSocket(0).run {
                use {
                    assert(it.localPort > 0)
                    return@run it.localPort
                }
            }

        } catch (e: IOException) {
            openErrorWindow {
                "Нет свободного порта"
            }
        }
        return 0
    }

    @Throws(IOException::class)
    fun stop() {
        serverSocket.close()
    }
}

