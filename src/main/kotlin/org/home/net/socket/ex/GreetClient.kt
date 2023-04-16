package org.home.net.socket.ex

import org.home.utils.threadPrintln
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class GreetClient {
    private lateinit var socket: Socket

    lateinit var writer: PrintWriter
    lateinit var reader: BufferedReader

    fun connect(ip: String, port: Int): GreetClient {
        socket = Socket(ip, port)
        this.writer = PrintWriter(socket.getOutputStream(), true)
        this.reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        return this
    }

    fun sendAndReceive(msg: String): String {
        threadPrintln("$sendSign \"$msg\"")
        this.writer.print(msg)
        this.writer.print("\n")
        this.writer.flush()
        val response = this.reader.readLine()
        threadPrintln("$receiveSign \"$response\"")
        return response
    }

    fun stop() {
        this.writer.close()
        this.reader.close()
        socket.close()
    }
}