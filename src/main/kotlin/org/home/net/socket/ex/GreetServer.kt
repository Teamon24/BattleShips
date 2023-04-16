package org.home.net.socket.ex

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class GreetServer {
    private lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: Socket
    private lateinit var out: PrintWriter
    private lateinit var `in`: BufferedReader

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        clientSocket = serverSocket.accept()
        out = PrintWriter(clientSocket.getOutputStream(), true)
        `in` = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val greeting = `in`.readLine()
        println("server received message: $greeting")
        if ("hello server" == greeting) {
            out.println("hello client")
        } else {
            out.println("message \"$greeting\" was received")
        }
    }

    fun stop() {
        `in`.close()
        out.close()
        clientSocket.close()
        serverSocket.close()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val server = GreetServer()
            server.start(6666)
        }
    }
}