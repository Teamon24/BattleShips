package org.home.net

import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.net.SocketOption
import java.nio.channels.SocketChannel

class PlayerSocket(private val socket: Socket) : Socket() {
    var player: String? = null

    override fun toString(): String {
        try {
            if (isConnected)
                return "$player ($inetAddress:$port <-> ${socket.localSocketAddress})"
        } catch (_: SocketException) {}
        return "PlayerSocket[unconnected]"

    }
    override fun close() = socket.close()

    override fun connect(endpoint: SocketAddress?) = socket.connect(endpoint)

    override fun connect(endpoint: SocketAddress?, timeout: Int) = socket.connect(endpoint, timeout)

    override fun bind(bindpoint: SocketAddress?) = socket.bind(bindpoint)

    override fun getInetAddress(): InetAddress = socket.inetAddress
    override fun getLocalAddress(): InetAddress = socket.localAddress
    override fun getPort() = socket.port
    override fun getLocalPort() = socket.localPort
    override fun getRemoteSocketAddress(): SocketAddress = socket.remoteSocketAddress
    override fun getLocalSocketAddress(): SocketAddress = socket.localSocketAddress
    override fun getChannel(): SocketChannel = socket.channel
    override fun getInputStream(): InputStream = socket.getInputStream()
    override fun getOutputStream(): OutputStream = socket.getOutputStream()

    override fun setTcpNoDelay(on: Boolean) { socket.tcpNoDelay = on }

    override fun getTcpNoDelay() = socket.tcpNoDelay
    override fun setSoLinger(on: Boolean, linger: Int) = socket.setSoLinger(on, linger)

    override fun getSoLinger() = socket.soLinger
    override fun sendUrgentData(data: Int) = socket.sendUrgentData(data)

    override fun setOOBInline(on: Boolean) { socket.oobInline = on }

    override fun getOOBInline() = socket.oobInline
    override fun setSoTimeout(timeout: Int) { socket.soTimeout = timeout }

    override fun getSoTimeout() = socket.soTimeout
    override fun setSendBufferSize(size: Int) { socket.sendBufferSize = size }

    override fun getSendBufferSize() = socket.sendBufferSize
    override fun setReceiveBufferSize(size: Int) { socket.receiveBufferSize = size }

    override fun getReceiveBufferSize() = socket.receiveBufferSize
    override fun setKeepAlive(on: Boolean) { socket.keepAlive = on }

    override fun getKeepAlive() = socket.keepAlive
    override fun setTrafficClass(tc: Int) { socket.trafficClass = tc }

    override fun getTrafficClass() = socket.trafficClass
    override fun setReuseAddress(on: Boolean) { socket.reuseAddress = on }

    override fun getReuseAddress() = socket.reuseAddress
    override fun shutdownInput() = socket.shutdownInput()

    override fun shutdownOutput() = socket.shutdownOutput()

    override fun isConnected() = socket.isConnected
    override fun isBound() = socket.isBound
    override fun isClosed() = socket.isClosed
    override fun isInputShutdown() = socket.isInputShutdown
    override fun isOutputShutdown() = socket.isOutputShutdown

    override fun setPerformancePreferences(connectionTime: Int, latency: Int, bandwidth: Int) = socket.setPerformancePreferences(connectionTime, latency, bandwidth)

    override fun <T : Any?> setOption(name: SocketOption<T>?, value: T): Socket
    = socket.setOption(name, value)
    override fun <T : Any?> getOption(name: SocketOption<T>?): T = socket.getOption(name)
    override fun supportedOptions(): MutableSet<SocketOption<*>?> = socket.supportedOptions()
}

val Socket.isNotClosed get() = !isClosed