package org.home.net.server

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.net.message.Message
import org.home.utils.SocketsMessages
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.log
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

abstract class MultiServer<M : Message, S : Socket>(
    protected val processor: MessageProcessor<M, S>,
    protected val receiver: MessageReceiver<M, S>,
    protected val accepter: ConnectionsListener<M, S>,
    appProps: ApplicationProperties
) : BattleController(appProps) {
    internal val readTimeout = 50

    internal val sockets = ConcurrentLinkedQueue<S>()
    internal val socketsMessages = SocketsMessages<M, S>()
    protected lateinit var serverSocket: ServerSocket


    internal val canAccept = AtomicBoolean(true)
    internal val canReceive = AtomicBoolean(true)
    internal val canProcess = AtomicBoolean(true)

    internal val canNotProcess get() = canProcess().not()

    internal fun stopToAccept() = canAccept(false)
    internal fun stopToReceive() = canReceive(false)
    internal fun stopToProcess() = canProcess(false)

    abstract fun process(socket: S, message: M)
    abstract fun accept(): S
    abstract fun onDisconnect(socket: S)

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        accepter.start()
        receiver.start()
        processor.start()
        thread {
            while (true) {
                Thread.sleep(3000)
                println("processor: isAlive/isInterrupted: ${processor.thread.isAlive}/${processor.thread.isInterrupted}")
                println("receiver: isAlive/isInterrupted: ${receiver.thread.isAlive}/${receiver.thread.isInterrupted}")
                println("accepter: isAlive/isInterrupted: ${accepter.thread.isAlive}/${accepter.thread.isInterrupted}")
            }
        }
    }

    private var acceptNextConnection = CountDownLatch(1)

    internal fun acceptNextConnection() = acceptNextConnection

    fun permitToAccept(value: Boolean) {
        log { "accepter can accept: $value" }
        canAccept(value)
        acceptNextConnection.countDown()
        acceptNextConnection = CountDownLatch(1)
        log { "reset barrier" }
    }

    fun runAccepter() {
        canAccept(true)
        accepter.start()
    }
}

