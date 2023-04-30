package org.home.utils

import org.home.mvc.contoller.events.DSLContainer
import org.home.net.action.Action
import org.home.net.message.Message
import org.home.net.message.MessagesDSL.Messages
import org.home.net.message.MessagesDSL.Messages.Companion.withInfo
import org.home.utils.IOUtils.read
import org.home.utils.IOUtils.write
import java.net.Socket

object SocketUtils {

    fun <T: Message> Socket.receive() = getInputStream().read<T>()
    fun <T: Message> Socket.send(messages: Messages<T>) = logSend(this) { getOutputStream().write(messages) }

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

    fun Collection<Socket>.send(addMessages: DSLContainer<Action>.() -> Unit) {
        val dslContainer = DSLContainer<Action>()
        dslContainer.addMessages()
        send(dslContainer.elements)
    }

    fun Socket.send(addMessages: DSLContainer<Action>.() -> Unit) {
        val dslContainer = DSLContainer<Action>()
        dslContainer.addMessages()
        send(dslContainer.elements)
    }
}