package org.home.utils

import org.apache.commons.lang3.SerializationUtils
import org.home.net.Message
import org.home.net.Messages
import org.home.net.MessagesDSL.wrap
import org.home.net.MessagesInfo
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

object MessageIO {

    fun <T: Message> OutputStream.write(message: T) {
        DataOutputStream(this).writeBatch(message.wrap())
    }

    fun <T: Message> OutputStream.write(messages: Messages<T>) {
        DataOutputStream(this).writeBatch(messages)
    }

    private fun <T: Message> OutputStream.writeBatch(messages: Messages<T>) {
        val out = DataOutputStream(this)
        messages.forEach {
            out.writeT(it)
        }
        out.flush()
    }

    fun <T: Message> InputStream.readBatch(): MutableList<T> {
        val `in` = DataInputStream(BufferedInputStream(this))
        val messages = mutableListOf<T>()
        val messagesInfo = `in`.readT<T>() as MessagesInfo
        for (i in 1..messagesInfo.number) {
            `in`.readT<T>().let {
                messages.add(it)
                logReceive { it }
            }
        }

        return messages
    }

    fun <T: Message> InputStream.readAll(): MutableList<T> {
        val `in` = DataInputStream(BufferedInputStream(this))
        val messages = mutableListOf<T>()
        try {
            var size = `in`.readInt()
            log { "received size = $size" }

            while (size > 0) {
                val messageBytes = `in`.readNBytes(size)
                log { "readed bytes: size = ${messageBytes.size}" }
                val message = SerializationUtils.deserialize<T>(messageBytes)
                logReceive { message }
                messages.add(message)
                size = `in`.readInt()
                log { "received size = $size" }
            }
            log { "while-loop reading is closed" }
        } catch (throwable: Throwable) {
            log {
                throwable.message ?:
                "no exception message in MessageIO#readAll"
            }
        }

        return messages
    }

    private fun <T : Message> DataInputStream.readT(): T {
        val size = readInt()
        val bytes = readNBytes(size)
        return SerializationUtils.deserialize(bytes)
    }

    private fun <T: Message> DataOutputStream.writeT(t: T) {
        val dataInBytes: ByteArray = SerializationUtils.serialize(t)
        writeInt(dataInBytes.size)
        write(dataInBytes)
    }
}
