package org.home.net.server

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.net.message.Message
import org.home.utils.SocketsMessages
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.log
import org.home.utils.threadPrintln
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

    abstract fun process(socket: S, message: M)
    abstract fun accept(): S
    abstract fun onDisconnect(socket: S)

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        accepter.start()
        receiver.start()
        processor.start()
        thread {
            while (false) {
                Thread.sleep(3000)
                threadPrintln(
                    "processor: isAlive/isInterrupted: ${processor.thread.isAlive}/${processor.thread.isInterrupted}")

                threadPrintln(
                    "receiver:  isAlive/isInterrupted: ${receiver.thread.isAlive} /${receiver.thread.isInterrupted}")
                threadPrintln(
                    "accepter:  isAlive/isInterrupted: ${accepter.thread.isAlive} /${accepter.thread.isInterrupted}")
            }
        }
    }

    private var acceptNextConnection = CountDownLatch(1)

    internal fun acceptNextConnection() = acceptNextConnection

    fun permitToAccept(value: Boolean) {
        log { "accepter can accept: $value" }
        accepter.canProceed(value)
        acceptNextConnection.countDown()
        acceptNextConnection = CountDownLatch(1)
        log { "reset barrier" }
    }

    fun runAccepter() {
        accepter.canProceed(true)
        accepter.start()
    }
}

