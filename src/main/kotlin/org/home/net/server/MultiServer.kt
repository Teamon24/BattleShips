package org.home.net.server

import org.home.mvc.contoller.BattleController
import org.home.net.accepter
import org.home.net.message.Message
import org.home.net.processor
import org.home.net.receiver
import org.home.utils.SocketsMessages
import org.home.utils.log
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

abstract class MultiServer<T : Message, S : Socket> : BattleController() {
    internal val readTimeout = 50

    internal val sockets = ConcurrentLinkedQueue<S>()
    internal val canAccept = AtomicBoolean(true)
    protected val processor = processor()
    protected val receiver = receiver()
    protected val accepter = accepter()

    protected lateinit var serverSocket: ServerSocket
    internal val socketMessagesQueue = SocketsMessages<T, S>()

    abstract fun process(socket: S, message: T)
    abstract fun accept(): S
    abstract fun onDisconnect(socket: S)

    init {
        processor.start()
        receiver.start()
    }

    private fun print() {
        println("          isActive / isCancelled / isCompleted")
        println("processor ${processor.isAlive}   / ${processor.isInterrupted}")
        println("receiver  ${receiver.isAlive}   / ${receiver.isInterrupted}  ")
        println("accepter  ${accepter.isAlive}   / ${accepter.isInterrupted}  ")
        println()
    }

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        accepter.start()
    }

    private var acceptNextBarrier: CountDownLatch = CountDownLatch(1)

    fun waitForAcceptPermission() {
        log { "wait for permission on accept" }
        acceptNextBarrier.await()
        log { "permission granted" }
    }

    fun permitToAccept(value: Boolean) {
        log { "accepter can accept: $value" }
        canAccept.set(value)
        acceptNextBarrier.countDown()
        acceptNextBarrier = CountDownLatch(1)
        log { "reset barrier" }
    }
}

