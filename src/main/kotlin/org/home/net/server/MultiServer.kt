package org.home.net.server

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.net.message.Message
import org.home.utils.SocketsMessages
import org.home.utils.log
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

abstract class MultiServer<M : Message, S : Socket>(
    protected val processor: MessageProcessor<M, S>,
    protected val receiver: MessageReceiver<M, S>,
    protected val accepter: ConnectionsListener<M, S>,
    appProps: ApplicationProperties
) : BattleController(appProps) {
    internal val readTimeout = 50

    protected lateinit var serverSocket: ServerSocket
    internal val socketsMessages = SocketsMessages<M, S>()

    internal val sockets = ConcurrentLinkedQueue<S>()
    internal val canAccept = AtomicBoolean(true)

    abstract fun process(socket: S, message: M)
    abstract fun accept(): S
    abstract fun onDisconnect(socket: S)

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        accepter.start()
        receiver.start()
        processor.start()
    }

    internal var acceptNextConnection = CountDownLatch(1)

    fun permitToAccept(value: Boolean) {
        log { "accepter can accept: $value" }
        canAccept.set(value)
        acceptNextConnection.countDown()
        acceptNextConnection = CountDownLatch(1)
        log { "reset barrier" }
    }
}

