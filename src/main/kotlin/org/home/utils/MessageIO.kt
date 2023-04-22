package org.home.utils

import org.apache.commons.lang3.SerializationUtils
import org.home.utils.SocketUtils.receiveSign
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

object MessageIO {

    fun <T: Serializable> OutputStream.write(t: T) {
        val out = DataOutputStream(this)
        val dataInBytes: ByteArray = SerializationUtils.serialize(t)
        out.writeInt(dataInBytes.size)
        out.write(dataInBytes)
    }

    fun <T: Serializable> InputStream.read(): T {
        val `in` = DataInputStream(BufferedInputStream(this))
        val size = `in`.readInt()
        val messageBytes = `in`.readNBytes(size)
        val message = SerializationUtils.deserialize<T>(messageBytes)
        return message
    }

    fun <T: Serializable> InputStream.readAll(): MutableList<T> {
        val `in` = DataInputStream(BufferedInputStream(this))
        val messages = mutableListOf<T>()
        try {
            var size = `in`.readInt()
            log { "#readAll: size = $size" }
            while (size > 0) {
                val messageBytes = `in`.readNBytes(size)
                val message = SerializationUtils.deserialize<T>(messageBytes)
                messages.add(message)
                size = `in`.readInt()
                log { "#readAll: size = $size" }
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        messages.forEach {
            log { "$receiveSign $it" }
        }
        return messages
    }
}