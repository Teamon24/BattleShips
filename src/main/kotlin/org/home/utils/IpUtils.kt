package org.home.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URL


object IpUtils {

    fun localIp(): String {
        val socket = Socket()
        socket.connect(InetSocketAddress("google.com", 80))
        return socket.localAddress.toString().replace("/", "")
    }

    fun publicIp(): String {
        val urlString = "https://checkip.amazonaws.com/"
        val url = URL(urlString)
        BufferedReader(InputStreamReader(url.openStream())).use { br -> return br.readLine() }
    }

    fun freePort(): Int {
        var port = 0
        try {
            ServerSocket(0).use { serverSocket ->
                assert(serverSocket.localPort > 0)
                port = serverSocket.localPort
            }
        } catch (e: IOException) {
            assert(false) { "Port is not available: ${e.message}" }
        }
        assert(port != 0) { "Port is not available" }
        return port
    }
}
