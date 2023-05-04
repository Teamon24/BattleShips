package org.home.net.server

import org.home.net.isNotClosed
import org.home.net.message.Message
import org.home.utils.InfiniteTry.Companion.infiniteTry
import org.home.utils.InfiniteTryBase.Companion.catch
import org.home.utils.InfiniteTryBase.Companion.handle
import org.home.utils.InfiniteTryBase.Companion.doWhile
import org.home.utils.InfiniteTryBase.Companion.noHandle
import org.home.utils.InfiniteTryFor
import org.home.utils.InfiniteTryFor.Companion.infiniteTryFor
import org.home.utils.SocketUtils.receive
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.removeFrom
import org.home.utils.extensions.AtomicBooleansExtensions.atomic
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.so
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

sealed class MultiServerThread<M: Message, S: Socket>: Controller() {
    abstract val name: String
    protected val multiServer: MultiServer<M, S> by di()
    lateinit var thread: Thread
    internal val canProceed = true.atomic
    abstract fun run()

    fun start() {
        thread = thread(start = false, name = name, block = this::run)
            .apply {
                start()
                logTitle("$name is STARTED")
            }
    }
    fun interrupt() {
        thread.interrupt()
    }
}


class ConnectionsListener<M: Message, S: Socket>: MultiServerThread<M, S>() {
    override val name get() = "connections-listener"

    override fun run() {
        multiServer {
            infiniteTry {
                sockets.add(accept().withTimeout(readTimeout))
                acceptNextConnection().await()
            } catch {
                +IOException::class
                +SocketException::class
                handle { logError(it) }

                +InterruptedException::class
                handle { canProceed(false) }
            } doWhile canProceed
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MessageReceiver<M: Message, S: Socket>: MultiServerThread<M, S>() {
    override val name get() = "receiver"

    override fun run() {
        multiServer {
            sockets.infiniteReceive { socket, received ->
                Thread.sleep(10)
                logReceive(socket, received)
                socketsMessages.add(socket to received.drop(1) as Collection<M>)
            } catch {
                +SocketTimeoutException::class
                noHandle()

                +SocketException::class
                +EOFException::class
                handle { ex, socket ->
                    logError(ex)
                    socket {
                        removeFrom(sockets)
                        isNotClosed.so { close() }
                    }
                    sockets.isEmpty().so { canProceed(false) }
                }

                +InterruptedException::class
                handle { _, _ -> canProceed(false) }
            } doWhile canProceed
        }
    }
}

class MessageProcessor<M: Message, S: Socket>: MultiServerThread<M, S>() {
    override val name get() = "processor"

    override fun run() {
        multiServer {
            infiniteTry {
                val (socket, messages) = socketsMessages.take()
                messages.forEach { process(socket, it) }
            } catch {
                +InterruptedException::class
                handle { canProceed(false) }
            } doWhile canProceed
        }
    }
}

private fun <S : Socket> S.withTimeout(readTimeout: Int): S {
    soTimeout = readTimeout
    return this
}

infix fun <S: Socket> Collection<S>.infiniteReceive(forEach: (S, Collection<Message>) -> Unit): InfiniteTryFor<S> {
    return infiniteTryFor { socket ->
        socket.receive {
            forEach(socket, it)
        }
    }
}


