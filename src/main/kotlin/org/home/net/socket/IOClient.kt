package org.home.net.socket

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket


abstract class IOClient<T> {

    private lateinit var clientSocket: Socket
    protected open lateinit var output: OutputStream
    protected open lateinit var input: InputStream

    open fun connect(ip: String, port: Int): IOClient<T> {
        clientSocket = Socket(ip, port)
        this.output = clientSocket.getOutputStream()
        this.input = clientSocket.getInputStream()
        return this
    }

    abstract fun sendAndReceive(msg: T)

    fun stop() {
        this.output.close()
        this.input.close()
        clientSocket.close()
    }
}