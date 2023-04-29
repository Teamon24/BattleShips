package org.home.net.socket.ex.nio

import org.home.utils.logReceive
import org.home.utils.logSend
import java.io.IOException
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class NioEchoClient {

    private var client: SocketChannel = SocketChannel.open(address)
    private var byteBuffer = byteBuffer()

    fun port(): SocketAddress? {
        return client.localAddress
    }

    fun sendMessage(msg: String): String {

        var response: String
        try {
            bufferedWrite(msg)
            logSend { msg }
            response = readString()
            logReceive { response }
        } catch (e: IOException) {
            e.printStackTrace()
            response = "${e.message}"
        }
        return response
    }

    private fun bufferedWrite(msg: String) {
        client.write(ByteBuffer.wrap(msg.toByteArray()))
    }

    private fun readString(): String {
        client.read(byteBuffer)
        return String(byteBuffer.array()).trim { it <= ' ' }
    }


    @Throws(IOException::class)
    fun stop() {
        client.close()
        byteBuffer.clear()
    }
}