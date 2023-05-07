package org.home.net.server

import org.home.net.isNotClosed
import org.home.net.message.Message
import org.home.utils.InfiniteTry.Companion.loop
import org.home.utils.InfiniteTryBase.Companion.catch
import org.home.utils.InfiniteTryBase.Companion.doWhile
import org.home.utils.InfiniteTryBase.Companion.handle
import org.home.utils.InfiniteTryBase.Companion.ignore
import org.home.utils.InfiniteTryBase.Companion.stopOn
import org.home.utils.InfiniteTryFor
import org.home.utils.InfiniteTryFor.Companion.infiniteTryFor
import org.home.utils.SocketUtils.receive
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.removeFrom
import org.home.utils.extensions.AtomicBooleansExtensions.atomic
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.invoke
import org.home.utils.log
import org.home.utils.logError
import org.home.utils.logReceive
import org.home.utils.logTitle
import tornadofx.Controller
import java.io.EOFException
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

sealed class MultiServerThread<M: Message, S: Socket>: Controller() {
    abstract val name: String
    protected val multiServer: MultiServer<M, S> by di()
    private lateinit var thread: Thread
    internal val canProceed = true.atomic
    abstract fun run()

    fun start() {
        canProceed(true)
        thread = thread(start = false, name = name, block = this::run)
            .apply {
                start()
                logTitle("$name is STARTED")
            }
    }
    fun interrupt() {
        canProceed(false)
        thread.interrupt()
    }

    val isAlive get() = thread.isAlive
    val isInterrupted get() = thread.isInterrupted

    fun onSocketException(socket: S) = multiServer.onDisconnect(socket)
}


class ConnectionsListener<M: Message, S: Socket>: MultiServerThread<M, S>() {
    override val name get() = "connector"

    override fun run() {
        multiServer {
            loop {
                sockets.add(accept().withTimeout(readTimeout))
                log { "waiting for connection permission ..." }
                connectionBarrier().await()
                log { "connection permission is granted" }
            } stopOn {
                InterruptedException::class
            } catch {
                +IOException::class
                +SocketException::class
                handle { logError(it) }
            } doWhile canProceed
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MessageReceiver<M: Message, S: Socket>: MultiServerThread<M, S>() {
    override val name get() = "receiver"

    override fun run() {
        multiServer {
            sockets.receiveInLoop { socket, messages ->
                logReceive(socket, messages)
                socketsMessages.add(socket to messages.drop(1) as Collection<M>)
            } ignore {
                SocketTimeoutException::class
            } stopOn {
                InterruptedException::class
            } catch {
                +SocketException::class
                +EOFException::class
                handle { ex, socket ->
                    logError(ex)
                    socket {
                        removeFrom(sockets)
                        isNotClosed { close() }
                    }
                    onSocketException(socket)
                }
            } doWhile canProceed
        }
    }
}

class MessageProcessor<M: Message, S: Socket>: MultiServerThread<M, S>() {
    override val name get() = "processor"

    override fun run() {
        multiServer {
            loop {
                val (socket, messages) = socketsMessages.take()

                socket.isNotClosed {
                    messages.forEach { message -> process(socket, message) }
                }
            } stopOn {
                InterruptedException::class
            } doWhile canProceed
        }
    }
}

private fun <S : Socket> S.withTimeout(readTimeout: Int): S {
    soTimeout = readTimeout
    return this
}

infix fun <S: Socket> Collection<S>.receiveInLoop(forEach: (S, Collection<Message>) -> Unit): InfiniteTryFor<S> {
    return infiniteTryFor { socket ->
        socket.receive {
            forEach(socket, it)
        }
    }
}


