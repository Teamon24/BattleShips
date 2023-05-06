package org.home.net.server

import org.home.mvc.contoller.BattleController
import org.home.net.message.Message
import org.home.utils.SocketsMessages
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.isNotAlive
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.log
import org.home.utils.logMultiServerThreads
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch

abstract class MultiServer<M : Message, S : Socket> : BattleController() {
    val processor: MessageProcessor<M, S> by di()
    val receiver: MessageReceiver<M, S> by di()
    val accepter: ConnectionsListener<M, S> by di()

    val threads = listOf(accepter, receiver, processor)

    internal val readTimeout = 50

    internal val sockets = ConcurrentLinkedQueue<S>()
    internal val socketsMessages = SocketsMessages<M, S>()

    private lateinit var serverSocket: ServerSocket

    protected fun serverSocket(): ServerSocket = serverSocket

    abstract fun process(socket: S, message: M)
    abstract fun accept(): S
    abstract fun onDisconnect(socket: S)

    fun start(port: Int) {
        serverSocket = ServerSocket(port)

        accepter.start()
        receiver.start()
        processor.start()

        logMultiServerThreads()
    }

    private var acceptNextConnection = CountDownLatch(1)

    internal fun acceptNextConnection() = acceptNextConnection

    fun permitToAccept(canConnect: Boolean) {
        log { "accepter can accept: $canConnect" }
        acceptNextConnection.countDown()
        acceptNextConnection = CountDownLatch(1)
        log { "reset barrier" }
    }

    fun runAccepter() {
        accepter.canProceed(true)
        accepter.start()
    }

    fun hasOnePlayer() = model.hasOnePlayerLeft()
}

