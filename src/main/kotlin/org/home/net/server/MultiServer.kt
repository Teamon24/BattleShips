package org.home.net.server

import org.home.mvc.contoller.BattleController
import org.home.net.message.Message
import org.home.utils.SocketsMessages
import org.home.utils.log
import org.home.utils.logMultiServerThreads
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch

abstract class MultiServer<M : Message, S : Socket> : BattleController() {
    val processor: MessageProcessor<M, S> by di()
    val receiver: MessageReceiver<M, S> by di()
    val connector: ConnectionsListener<M, S> by di()

    val threads = listOf(connector, receiver, processor)

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

        connector.start()
        receiver.start()
        processor.start()

        logMultiServerThreads(false)
    }

    private var connectionBarrier = CountDownLatch(1)

    internal fun connectionBarrier() = connectionBarrier

    fun permitToConnect() {
        connectionBarrier.countDown()
        connectionBarrier = CountDownLatch(1)
        log { "reset barrier" }
    }
}

