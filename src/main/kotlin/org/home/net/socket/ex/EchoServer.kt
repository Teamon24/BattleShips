package org.home.net.socket.ex

import org.home.utils.threadPrintln
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.util.Random
import kotlin.concurrent.thread


class EchoServer {

    fun start(port: Int) {
        val serverSocket = ServerSocket(port)
        val clientSocket = serverSocket.accept()
        handle(clientSocket.getInputStream(), clientSocket.getOutputStream())
    }

    fun handle(inputStream: InputStream, outputStream: OutputStream) {
        val writer = PrintWriter(outputStream, true)
        val reader = BufferedReader(InputStreamReader(inputStream))
        var inputLine: String
        threadPrintln("waiting for a message")
        var result = reader.readLine()
        while (result.also { inputLine = it } != null) {
            threadPrintln("$receiveSign \"$inputLine\"")
            if ("." == inputLine) {
                writer.println("good bye, client")
                break
            }
            writer.println("I have read \'$inputLine\'")
            result = reader.readLine()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val port = Random().nextInt(1000, 2000)
            thread { EchoServer().start(port) }
            Thread.sleep(1000)
            val client = GreetClient().connect("127.0.0.1", port)

            client.sendAndReceive("hello")
            client.sendAndReceive("world")
            client.sendAndReceive("!")
            client.sendAndReceive(".")
        }
    }
}
