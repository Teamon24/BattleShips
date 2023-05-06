package org.home.utils

import org.home.net.isNotClosed
import org.home.net.message.Action
import org.home.net.message.Message
import org.home.net.message.MessagesDSL.Messages
import org.home.net.message.MessagesDSL.Messages.Companion.withInfo
import org.home.utils.IOUtils.readBatch
import org.home.utils.IOUtils.write
import org.home.utils.extensions.BooleansExtensions.so
import java.net.Socket

object SocketUtils {

    fun Socket.receive() = getInputStream().readBatch()

    fun Socket.receive(onRead: (Collection<Message>) -> Unit) =
        isNotClosed.so {
            onRead(getInputStream().readBatch())
        }

    fun <T: Message> Socket.send(messages: Messages<T>) =
        isNotClosed.so {
            logSend(this) {
                getOutputStream().write(messages)
            }
        }

    fun <T: Message> Socket.send(message: T) = send(withInfo(message))
    fun <T: Message> Socket.send(messages: Collection<T>) = send(withInfo(messages))

    fun <T: Message, S: Socket> Collection<S>.send(message: T) = forEach { it.send(withInfo(message)) }
    fun <T: Message, S: Socket> Collection<S>.send(messages: Collection<T>) = forEach { it.send(messages) }
    fun <T: Message, S: Socket> Collection<S>.send(messages: Messages<T>) = forEach { it.send(messages) }

    fun <S: Socket> send(socketsAndMessages: Map<S, MutableList<Message>>) {
        socketsAndMessages
            .map { it.key to withInfo(it.value) }
            .forEach { (socket, messages) -> socket.send(messages) }
    }

    inline infix fun Collection<Socket>.send(addMessages: DSLContainer<Action>.() -> Unit) {
        val dslContainer = DSLContainer<Action>()
        dslContainer.addMessages()
        send(dslContainer.elements)
    }

    inline fun Socket.send(addMessages: DSLContainer<Action>.() -> Unit) {
        val dslContainer = DSLContainer<Action>()
        dslContainer.addMessages()
        send(dslContainer.elements)
    }
}