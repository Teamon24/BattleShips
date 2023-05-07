package org.home.utils

import org.home.net.isNotClosed
import org.home.net.message.Action
import org.home.net.message.Message
import org.home.net.message.MessagesDSL.Messages
import org.home.net.message.MessagesDSL.Messages.Companion.withInfo
import org.home.utils.IOUtils.readBatch
import org.home.utils.IOUtils.write
import org.home.utils.extensions.BooleansExtensions.invoke
import java.net.Socket

object SocketUtils {

    fun Socket.receive() = getInputStream().readBatch()

    fun Socket.receive(onRead: (Collection<Message>) -> Unit) =
        isNotClosed {
            onRead(getInputStream().readBatch())
        }

    fun <T: Message> Socket.send(messages: Messages<T>) =
        isNotClosed {
            logSend(this) {
                getOutputStream().write(messages)
            }
        }

    fun Socket.send(message: Message) = send(withInfo(message))
    fun Socket.send(messages: Collection<Message>) = send(withInfo(messages))

    fun <T: Message, S: Socket> Collection<S>.send(message: T) = forEach { it.send(withInfo(message)) }
    fun <T: Message, S: Socket> Collection<S>.send(messages: Collection<T>) = forEach { it.send(messages) }
    fun <T: Message, S: Socket> Collection<S>.send(messages: Messages<T>) = forEach { it.send(messages) }

    fun <S: Socket> send(socketsAndMessages: Map<S, MutableList<Message>>) {
        socketsAndMessages
            .map { it.key to withInfo(it.value) }
            .forEach { (socket, messages) -> socket.send(messages) }
    }

    inline infix fun Collection<Socket>.send(addMessages: DSLContainer<Message>.() -> Unit) {
        send(dslContainer(addMessages))
    }

    inline fun Socket.send(addMessages: DSLContainer<Message>.() -> Unit) {
        send(dslContainer(addMessages))
    }
}