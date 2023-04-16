package org.home.net

import kotlinx.coroutines.launch
import org.home.mvc.contoller.BattleController
import org.home.utils.fixedThreadPool
import org.home.utils.threadPrintln
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket


abstract class MultiServer: BattleController() {
    private lateinit var serverSocket: ServerSocket
    private val clients: MutableList<Socket> = mutableListOf()
    private val ioScope = fixedThreadPool(10, "SERVER")

    abstract suspend fun receive(`in`: InputStream, out: OutputStream)
    abstract suspend fun send(msg: Message, out: OutputStream)

    @Throws(IOException::class)
    fun start(port: Int) {
        serverSocket = ServerSocket(port)

        threadPrintln("server socket is created")

        while (true) {
            val client = serverSocket.accept()
            threadPrintln("client has been connected")

            clients.add(client)

            val input = client.getInputStream()
            val output = client.getOutputStream()

            ioScope.launch {
                receive(input, output)
                if (client.isClosed) {
                    clients.remove(client)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun stop() {
        serverSocket.close()
    }
}

