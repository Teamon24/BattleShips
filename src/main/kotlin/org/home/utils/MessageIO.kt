package org.home.utils

import org.apache.commons.lang3.SerializationUtils
import org.home.net.Message
import org.home.utils.MessageIO.read
import org.home.utils.SocketUtils.receiveSign
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

object MessageIO {

    fun <T: java.io.Serializable> OutputStream.write(t: T) {
        val out = DataOutputStream(this)
        val dataInBytes: ByteArray = SerializationUtils.serialize(t)
        out.writeInt(dataInBytes.size)
        out.write(dataInBytes)
    }

    fun <T> InputStream.read(): T {
        val `in` = DataInputStream(BufferedInputStream(this))
        val size = `in`.readInt()
        val messageBytes = `in`.readNBytes(size)
        val message = SerializationUtils.deserialize<T>(messageBytes)
        return message
    }

    fun InputStream.readAll(): MutableList<Message> {
        val `in` = DataInputStream(BufferedInputStream(this))
        val messages = mutableListOf<Message>()
        try {
            var size = `in`.readInt()
            log { "#readAll: size = $size" }
            while (size > 0) {
                val messageBytes = `in`.readNBytes(size)
                val message = SerializationUtils.deserialize<Message>(messageBytes)
                messages.add(message)
                size = `in`.readInt()
                log { "#readAll: size = $size" }
            }
        } catch (t: Throwable) {
            println(t.message)
        }
        messages.forEach {
            log { "$receiveSign $it" }
        }
        return messages
    }
}