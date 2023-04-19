package org.home.net

import kotlinx.coroutines.launch
import org.home.mvc.view.openErrorWindow
import org.home.utils.ioScope
import org.home.utils.singleThread
import org.home.utils.threadPrintln
import tornadofx.Controller
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket


abstract class MultiServer {
    private lateinit var serverSocket: ServerSocket
    protected val clients: MutableList<Socket> = mutableListOf()

    abstract suspend fun listen(`in`: InputStream, out: OutputStream)

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
    fun start() {
        start(getFreePort())
    }

    @Throws(IOException::class)
    fun start(port: Int) {
        serverSocket = ServerSocket(port)

        threadPrintln("server socket is created")
        singleThread {
            while (true) {
                val client = serverSocket.accept()
                threadPrintln("client has been connected")

                clients.add(client)

                ioScope.launch {
                    listen(
                        client.getInputStream(),
                        client.getOutputStream()
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    fun stop() {
        serverSocket.close()
    }
}

