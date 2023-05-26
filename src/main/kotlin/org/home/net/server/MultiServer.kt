package org.home.net.server

import org.home.app.di.noScope
import org.home.mvc.GameController
import org.home.mvc.contoller.server.PlayerSocket
import org.home.utils.log
import org.home.utils.logProps
import org.home.utils.logServerStart
import tornadofx.Component
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue

typealias SocketMessages<M, S> = Pair<S, Collection<M>>
typealias SocketsMessages<M, S> = LinkedBlockingQueue<SocketMessages<M, S>>
typealias PlayersSockets = ConcurrentLinkedQueue<PlayerSocket>

abstract class MultiServer<M : Message, S : Socket>: GameController() {

    class MultiServerSockets<S: Socket>: Component() {
        private val sockets = ConcurrentLinkedQueue<S>()
        fun get() = sockets
    }

    internal val processor         by noScope<MessageProcessor<M, S>>()
    internal val receiver          by noScope<MessageReceiver<M, S>>()
    internal val connector         by noScope<ConnectionsListener<M, S>>()
    private val multiServerSockets by noScope<MultiServerSockets<S>>()

    val threads = listOf(connector, receiver, processor)

    internal val readTimeout = 50

    internal val sockets = multiServerSockets.get()
    internal val socketsMessages = SocketsMessages<M, S>()

    private lateinit var serverSocket: ServerSocket

    fun serverSocket(): ServerSocket = serverSocket

    abstract fun process(socket: S, message: M)
    abstract fun accept(): S
    abstract fun onDisconnect(socket: S)

    fun start(port: Int) {
        log { "creating socket on $port" }
        serverSocket = ServerSocket(port)
        connector.start()
        receiver.start()
        processor.start()

        logServerStart()

        modelView.logProps()
    }

    private var connectionBarrier = CountDownLatch(1)

    internal fun connectionBarrier() = connectionBarrier

    fun permitToConnect() {
        connectionBarrier.countDown()
        connectionBarrier = CountDownLatch(1)
        log { "reset connection barrier" }
    }
}

