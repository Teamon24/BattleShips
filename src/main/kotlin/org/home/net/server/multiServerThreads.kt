package org.home.net.server

import org.home.net.isNotClosed
import org.home.net.message.Message
import org.home.utils.InfiniteTry.Companion.infiniteTry
import org.home.utils.InfiniteTryBase.Companion.catch
import org.home.utils.InfiniteTryBase.Companion.handle
import org.home.utils.InfiniteTryBase.Companion.start
import org.home.utils.InfiniteTryBase.Companion.stopLoop
import org.home.utils.InfiniteTryFor.Companion.infiniteTryFor
import org.home.utils.SocketUtils.receive
import org.home.utils.extensions.AnysExtensions.invoke
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
import kotlin.concurrent.thread

class ConnectionsListener<M: Message, S: Socket>: Controller() {
    private val multiServer: MultiServer<M, S> by di()
    private lateinit var thread: Thread

    fun start() { thread = createThread().apply { start() } }
    fun interrupt() { thread.interrupt() }

    private fun createThread() = thread(start = false, name = "ACCEPTER") {
        multiServer {
            while (canAccept.get()) {
                try {
                    val socket = accept().withTimeout(readTimeout)
                    sockets.add(socket)
                    acceptNextConnection.await()
                    log { "canAccept: ${canAccept.get()}" }
                } catch (e: IOException) {
                    logError(e)
                }
            }
        }
        logTitle("ACCEPTER") { "IM DONE" }
    }
}

@Suppress("UNCHECKED_CAST")
class MessageReceiver<M: Message, S: Socket>: Controller() {
    private val multiServer: MultiServer<M, S> by di()
    private lateinit var thread: Thread

    fun start() { thread = createThread().apply { start() } }
    fun interrupt() { thread.interrupt() }

    private fun createThread() = thread(start = false, name = "receiver") {
        multiServer {
            sockets.infiniteTryFor { socket ->
                socket.isNotClosed.so {
                    val messages = socket.receive()
                    logReceive(socket, messages)
                    socketsMessages.add(socket to messages.drop(1) as Collection<M>)
                }
            } catch {
                +SocketException::class
                +EOFException::class
                handle { ex, socket ->
                    sockets.handle(ex, socket)
                    sockets.isEmpty().so { stopLoop() }
                }
                +InterruptedException::class
                handle { ex, _ ->
                    logError(ex)
                    stopLoop()
                }

                +SocketTimeoutException::class
                handle { ex, _ -> handleTimeout(ex) }
            } start true
        }
    }
}

class MessageProcessor<M: Message, S: Socket>: Controller() {
    private val multiServer: MultiServer<M, S> by di()
    private lateinit var thread: Thread

    fun start() { thread = createThread().apply { start() } }
    fun interrupt() { thread.interrupt() }

    private fun createThread() = thread(start = false, name = "processor") {
        infiniteTry {
            multiServer {
                val (socket, messages) = socketsMessages.take()
                messages.forEach { process(socket, it) }
            }
        } catch {
            +InterruptedException::class
            handle { ex ->
                logError(ex, false)
                stopLoop()
            }
        } start true
    }


}

private fun <S : Socket> MutableCollection<S>.handle(e: Throwable, socket: S) {
    logError(e)
    remove(socket)
    socket.close()
}


private fun handleTimeout(ex: Exception) {
    log(disabled = true) {
        "socket has not sent any message: trying the next one"
    }
}

private fun <S : Socket> S.withTimeout(readTimeout: Int): S {
    soTimeout = readTimeout
    return this
}


