package org.home.utils

import org.home.net.Message
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

object SocketUtils {

    const val receiveSign = "<=="
    const val sendSign = "==>"
    fun OutputStream.sendAndReceive(msg: Message, `in`: InputStream) {
        send(msg)
        `in`.receive()
    }

    fun InputStream.receive(): Message {
        val msg = read<Message>()
        log { "$receiveSign $msg" }
        return msg
    }
    fun OutputStream.send(msg: Message) {
        write(msg)
        log { "$sendSign $msg" }
    }

    fun Map<Socket, String>.sendAll(message: Message) {
        forEach {
            logCom(it.value) {
                it.key.getOutputStream().send(message)
            }
        }
    }
}