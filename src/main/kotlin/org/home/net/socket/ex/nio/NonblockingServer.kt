package org.home.net.socket.ex.nio

import java.lang.Exception
import kotlin.Throws
import kotlin.jvm.JvmStatic
import java.net.InetAddress
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

object NonblockingServer {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val host = InetAddress.getByName("localhost")
        val selector = Selector.open()
        val serverSocketChannel = ServerSocketChannel.open()
        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress(host, 1234))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
        var key: SelectionKey
        while (true) {
            if (selector.select() <= 0) continue
            val selectedKeys = selector.selectedKeys()
            val iterator = selectedKeys.iterator()
            while (iterator.hasNext()) {
                key = iterator.next() as SelectionKey
                iterator.remove()
                if (key.isAcceptable) {
                    val sc = serverSocketChannel.accept()
                    sc.configureBlocking(false)
                    sc.register(selector, SelectionKey.OP_READ)
                    println("Connection Accepted: "
                            + sc.localAddress + "n")
                }
                if (key.isReadable) {
                    val sc = key.channel() as SocketChannel
                    val bb = ByteBuffer.allocate(1024)
                    sc.read(bb)
                    val result = String(bb.array()).trim { it <= ' ' }
                    println("Message received: "
                            + result
                            + " Message length= " + result.length)
                    if (result.length <= 0) {
                        sc.close()
                        println("Connection closed...")
                        println(
                            "Server will keep running. " +
                                    "Try running another client to " +
                                    "re-establish connection")
                    }

                    val toByteArray = "I have read '${result}'".toByteArray()
                    sc.write(ByteBuffer.wrap(toByteArray))
                }
            }
        }
    }
}