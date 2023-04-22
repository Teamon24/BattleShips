package org.home.utils

import org.home.net.Message
import org.home.net.Messages
import org.home.net.MessagesDSL.wrap
import org.home.net.PlayerSocket
import org.home.utils.MessageIO.readAll
import org.home.utils.MessageIO.readBatch
import org.home.utils.MessageIO.write
import java.net.Socket

object SocketUtils {

    fun <T: Message> Socket.receiveBatch() = getInputStream().readBatch<T>()

    fun <T: Message> Socket.receiveAll() = getInputStream().readAll<T>()

    fun <T: Message> Collection<PlayerSocket>.sendAll(message: T) {
        forEach { socket ->
            logCom(socket.player!!) {
                socket.send(message.wrap())
            }
        }
    }

    fun <T: Message, S: Socket> S.send(messages: Messages<T>) {
        getOutputStream().write(messages)
    }
}