package org.home.utils

import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.isNotEmpty
import org.home.net.server.Message
import org.home.net.server.MessagesDSL.Messages
import org.home.net.server.MessagesDSL.Messages.Companion.withInfo
import org.home.utils.IOUtils.readBatch
import org.home.utils.IOUtils.write
import java.net.Socket

object SocketUtils {

    val Socket.isNotClosed get() = !isClosed

    inline fun Socket.isNotClosed(onTrue: Socket.() -> Unit) {
        isNotClosed.so { onTrue() }
    }

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
    fun Socket.send(messages: Collection<Message>) = messages.isNotEmpty { send(withInfo(messages)) }

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