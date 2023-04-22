package org.home.utils

import org.home.net.PlayerSocket
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.readAll
import org.home.utils.MessageIO.write
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.net.Socket

object SocketUtils {

    const val receiveSign = "<=="
    const val sendSign = "==>"

    fun <T: Serializable> InputStream.receive(): T {
        val msg = read<T>()
        log { "$receiveSign $msg" }
        return msg
    }

    fun <T: Serializable> Socket.receiveAll() = getInputStream().readAll<T>()

    fun <T: Serializable> OutputStream.send(msg: T) {
        write(msg)
        log { "$sendSign $msg" }
    }

    fun <T: Serializable, S: Socket> Map<String, S>.sendAll(message: T) {
        forEach {
            logCom(it.key) {
                it.value.getOutputStream().send(message)
            }
        }
    }

    @JvmName("playerSocketsSendAll")
    fun <T: Serializable> Collection<PlayerSocket>.sendAll(message: T) {
        forEach {
            logCom(it.player!!) {
                it.outputStream.send(message)
            }
        }
    }

    fun <T: Serializable, S: Socket> S.sendAndReceive(msg: T) {
        getOutputStream().send(msg)
        getInputStream().receive<T>()
    }

    fun <T: Serializable, S: Socket> S.send(msg: T) {
        getOutputStream().send(msg)
    }
}