package org.home.net.socket.ex.nio

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.home.utils.extensions.threadsScope
import org.home.utils.logReceive
import org.home.utils.logSend
import org.home.utils.threadPrintln
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.CountDownLatch


object NioEchoServer {
    const val POISON_PILL = "POISON_PILL"
    val scope = threadsScope(10, "server-pool")

    @Throws(IOException::class)
    fun start(latch: CountDownLatch) {
        threadPrintln("starting...")
        val selector = Selector.open()
        val serverSocket = ServerSocketChannel.open()
        serverSocket.readyToAccept(address, selector)
        val byteBuffer = byteBuffer()

        while (true) {
            threadPrintln("waiting for connection...")
            latch.countDown()
            selector.select()
            threadPrintln("connection has been established...")
            val keys = selector.selectedKeys()
            val keysIterator = keys.iterator()

            while (keysIterator.hasNext()) {
                val key = keysIterator.next()

                when {
                    key.isAcceptable -> scope.launch { serverSocket.readyToRead(selector) }
                    key.isReadable -> answerWithEcho(byteBuffer, key)
                }
                keysIterator.remove()
            }
        }
    }

    private fun ServerSocketChannel.readyToAccept(
        address: InetSocketAddress,
        selector: Selector,
    ) {
        bind(address)
        configureBlocking(false)
        register(selector, SelectionKey.OP_ACCEPT)
    }

    @Throws(IOException::class)
    private fun ServerSocketChannel.readyToRead(selector: Selector) {
        val client = accept()
        client.configureBlocking(false)
        client.register(selector, SelectionKey.OP_READ)
    }

    @Throws(IOException::class)
    private fun answerWithEcho(byteBuffer: ByteBuffer, key: SelectionKey) {
        byteBuffer.clear()
        val client = key.channel() as SocketChannel
        client.read(byteBuffer)
        val message = readString(byteBuffer)
        logReceive { message }
        byteBuffer.clear()
        if (isPoisonPill(message)) {
            close(client)
        } else {
            val buffer = ByteBuffer.wrap("Ive read \"$message\"".toByteArray())
            client.writeTo(buffer)
            logSend { String(buffer.array()) }
        }
    }

    private fun readString(byteBuffer: ByteBuffer): String {
        val message = String(byteBuffer.array()).trim { it <= ' ' }
        return message
    }


    private fun isPoisonPill(message: String) = message == POISON_PILL

    private fun close(client: SocketChannel) {
        client.close()
        println("Not accepting client messages anymore")
    }

    private fun SocketChannel.writeTo(buffer: ByteBuffer) {
        buffer.flip()
        write(buffer)
        buffer.clear()
    }

    fun stop() {
        scope.cancel()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun asProcess(): ProcessBuilder {
        val javaHome = System.getProperty("java.home")
        val javaBin = listOf(javaHome, "bin", "java").joinToString(separator = File.separator)
        val classpath = System.getProperty("java.class.path")
        val className = NioEchoServer::class.java.canonicalName
        return ProcessBuilder(javaBin, "-cp", classpath, className)
    }
}