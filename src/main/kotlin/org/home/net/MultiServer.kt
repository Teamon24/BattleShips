package org.home.net

import kotlinx.coroutines.launch
import org.home.mvc.view.openErrorWindow
import org.home.utils.functions.threadsScope
import org.home.utils.log
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

abstract class MultiServer {
    private lateinit var serverSocket: ServerSocket
    internal val sockets: MutableMap<String, Socket> = mutableMapOf()
    private val threadsScope = threadsScope(Runtime.getRuntime().availableProcessors() * 2, "clients")

    abstract fun listen(socket: Socket)

    @Throws(IOException::class)
    fun start(port: Int) {
        serverSocket = ServerSocket(port)

        log { "server socket is created" }
        thread(name = "connection listener") {
            while (true) {
                val client = serverSocket.accept()
                log { "client has been connected" }
                threadsScope.launch {
                    listen(client)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun start() {
        start(getFreePort())
    }

    private fun getFreePort(): Int {
        try {
            ServerSocket(0).run {
                if (localPort > 0) return localPort
            }

        } catch (e: IOException) {
            openErrorWindow {
                "При выборе порта возникла ошибка"
            }
        }
        return 0
    }

    @Throws(IOException::class)
    fun stop() {
        serverSocket.close()
    }
}

