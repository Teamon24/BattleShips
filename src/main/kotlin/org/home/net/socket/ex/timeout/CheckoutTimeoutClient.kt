package org.home.net.socket.ex.timeout

import org.home.net.Message
import org.home.utils.MessageIO.write
import org.home.utils.SocketUtils.sendSign
import org.home.utils.log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException

class CheckoutTimeoutClient {
    private lateinit var `in`: InputStream
    private lateinit var out: OutputStream
    lateinit var socket: Socket
    val number = counter;

    init { counter++ }

    companion object { var counter = 0 }

    @Throws(UnknownHostException::class, IOException::class)
    fun connect(ip: String, port: Int): CheckoutTimeoutClient {
        socket = Socket(ip, port)
        out = socket.getOutputStream()
        `in` = socket.getInputStream()
        return this
    }

    fun send(msg: Message) {
        out.write(msg)
        log { "$sendSign \"$msg\"" }
    }

    fun close() {
        `in`.close()
        out.close()
        socket.close()
    }
}