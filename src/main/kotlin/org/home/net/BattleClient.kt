package org.home.net

import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.net.socket.ex.receiveSign
import org.home.net.socket.ex.sendSign
import org.home.utils.MessageIO.read
import org.home.utils.MessageIO.write
import org.home.utils.threadPrintln
import tornadofx.Controller
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class BattleClient(private val gameController: GameTypeController): BattleController() {

    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var socket: Socket

    fun connect(ip: String, port: Int) {
        socket = Socket(ip, port)
        output = socket.getOutputStream()
        input = socket.getInputStream()
    }

    fun send(msg: ActionMessage) {
        output.write(msg)
        threadPrintln("$sendSign \"$msg\"")
        receive()
    }

    private fun receive() {
        val response = input.read<ActionMessage>()
        threadPrintln("$receiveSign \"$response\"")
    }

    fun stop() {
        input.close()
        output.close()
        socket.close()
    }


}