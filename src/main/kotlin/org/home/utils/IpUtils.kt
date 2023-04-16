package org.home.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
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
}
