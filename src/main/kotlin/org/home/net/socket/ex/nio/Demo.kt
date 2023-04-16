package org.home.net.socket.ex.nio

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.utils.singleThreadScope
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

const val port = 5454
val address = InetSocketAddress("localhost", port)
val byteBuffer = { ByteBuffer.allocate(256) }

object Demo {
    @JvmStatic
    fun main(args: Array<String>) {
        val lock = CountDownLatch(1)
        singleThreadScope("server").launch {
            NioEchoServer.start(lock)
        }

        lock.await()
        (1..60).map {
            singleThreadScope("client").launch(start = CoroutineStart.LAZY) {
                runBlocking {
                    NioEchoClient().run {
                        println(port())
                        sendMessage(message(it))
                        stop()
                    }
                }
            }
        }.apply {
            runBlocking {
                onEach { it.start() }
                map { it.join() }
            }
        }

        NioEchoServer.stop()
    }

    private fun message(it: Any) = "hello, im client #$it"
}

