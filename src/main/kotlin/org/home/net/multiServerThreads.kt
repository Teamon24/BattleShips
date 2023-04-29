package org.home.net

import org.home.net.InfiniteTry.Companion.infiniteTry
import org.home.net.InfiniteTryBase.Companion.catch
import org.home.net.InfiniteTryBase.Companion.handle
import org.home.net.InfiniteTryBase.Companion.start
import org.home.net.InfiniteTryBase.Companion.stopLoop
import org.home.net.InfiniteTryFor.Companion.infiniteTryFor
import org.home.net.message.Message
import org.home.net.server.MultiServer
import org.home.utils.SocketUtils.receive
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.log
import org.home.utils.logError
import org.home.utils.logReceive
import org.home.utils.logTitle
import java.io.EOFException
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

internal fun <T : Message, S : Socket> MultiServer<T, S>.processor() = thread(start = false, name = "processor") {
    infiniteTry {
        val (socket, messages) = socketMessagesQueue.take()
        messages.forEach { process(socket, it) }
    } catch  {
        +InterruptedException::class
        handle { ex ->
            logError(ex, false)
            stopLoop()
        }
    } start true
}

internal fun <T : Message, S : Socket> MultiServer<T, S>.receiver() =
    thread(start = false, name = "receiver") {
        sockets.infiniteTryFor { socket ->
            socket.isNotClosed.so {
                val receivedMessages = socket.receive<T>()
                logReceive(socket) {
                    logReceive { receivedMessages }
                }
                socketMessagesQueue.add(socket to receivedMessages)
            }
        } catch {
            +SocketException::class
            +EOFException::class
            +InterruptedException::class
            handle { ex, socket -> sockets.handle(ex, socket) }

            +SocketTimeoutException::class
            handle { ex, _ -> handleTimeout(ex) }
        } start true
    }

internal fun <T : Message, S : Socket> MultiServer<T, S>.accepter() =
    thread(start = false, name = "ACCEPTER") {
        while (canAccept.get()) {
            try {
                val socket = accept().withTimeout(readTimeout)
                sockets.add(socket)
                waitForAcceptPermission()
                log { "canAccept: ${canAccept.get()}" }
            } catch (e: IOException) {
                logError(e)
            }
        }
        logTitle("ACCEPTER") { "IM DONE" }
    }

private fun <S : Socket> MutableCollection<S>.handle(e: Throwable, socket: S) {
    logError(e)
    remove(socket)
    socket.close()
}


private fun handleTimeout(ex: Exception) {
    log(disabled = true) {
        "socket has not sent any message: trying the next one"
    }
}

private fun <S : Socket> S.withTimeout(readTimeout: Int): S {
    soTimeout = readTimeout
    return this
}


