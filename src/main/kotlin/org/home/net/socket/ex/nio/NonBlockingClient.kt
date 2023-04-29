package org.home.net.socket.ex.nio

import java.io.BufferedReader
import kotlin.Throws
import kotlin.jvm.JvmStatic
import java.net.InetSocketAddress
import java.net.InetAddress
import java.nio.channels.SocketChannel
import java.nio.channels.SelectionKey
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.channels.Selector

object NonBlockingClient {
    private var ideaConsole: BufferedReader? = null
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val addr = InetSocketAddress(
            InetAddress.getByName("localhost"), 1234)
        val selector = Selector.open()
        val sc = SocketChannel.open()
        sc.configureBlocking(false)
        sc.connect(addr)
        sc.register(selector, SelectionKey.OP_CONNECT or
                SelectionKey.OP_READ or SelectionKey.OP_WRITE)
        ideaConsole = BufferedReader(InputStreamReader(System.`in`))
        while (true) {
            if (selector.select() > 0) {
                val doneStatus = processReadySet(selector.selectedKeys())
                if (doneStatus) {
                    break
                }
            }
        }
        sc.close()
    }

    @Throws(Exception::class)
    fun processReadySet(readySet: MutableSet<*>): Boolean {
        var key: SelectionKey? = null
        val iterator = readySet.iterator()
        while (iterator.hasNext()) {
            key = iterator.next() as SelectionKey?
            iterator.remove()
        }
        if (key!!.isConnectable) {
            val connected = processConnect(key)
            if (!connected) {
                return true
            }
        }
        if (key.isReadable) {
            val sc = key.channel() as SocketChannel
            val bb = ByteBuffer.allocate(1024)
            sc.read(bb)
            val result = toString(bb)
            println(
                "Message received from Server: $result Message length= ${result.length}")
        }

        if (key.isWritable) {
            print("Type a message (type quit to stop): ")
            val msg = ideaConsole!!.readLine()
            if (msg.equals("quit", ignoreCase = true)) {
                return true
            }
            val sc = key.channel() as SocketChannel
            val bb = ByteBuffer.wrap(msg.toByteArray())
            sc.write(bb)
        }
        return false
    }

    private fun toString(bb: ByteBuffer): String {
        val result = String(bb.array()).trim { it <= ' ' }
        return result
    }

    fun processConnect(key: SelectionKey?): Boolean {
        val sc = key!!.channel() as SocketChannel
        try {
            while (sc.isConnectionPending) {
                sc.finishConnect()
            }
        } catch (e: IOException) {
            key.cancel()
            e.printStackTrace()
            return false
        }
        return true
    }
}