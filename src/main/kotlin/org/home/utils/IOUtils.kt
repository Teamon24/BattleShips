package org.home.utils

import org.apache.commons.lang3.SerializationUtils
import org.home.net.message.Message
import org.home.net.message.MessagesDSL.Messages
import org.home.net.message.MessagesInfo
import org.home.utils.extensions.AnysExtensions.repeat
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

object IOUtils {

    fun <T: Message> OutputStream.write(messages: Messages<T>) {
        DataOutputStream(this).writeBatch(messages)
    }

    @JvmName("readMessages")
    fun <T: Message> InputStream.read(): Collection<T> {
        return DataInputStream(this).readBatch()
    }

    private fun <T: Message> DataInputStream.readBatch(): Collection<T> {
        val messages = mutableListOf<T>()
        val messagesInfo = readOne<T>() as MessagesInfo
        messagesInfo.number.repeat {
            messages.add(readOne())
        }
        return messages
    }

    private fun <T : Message> DataInputStream.readOne(): T {
        val size = readInt()
        val bytes = readNBytes(size)
        return SerializationUtils.deserialize(bytes)
    }

    private fun <T: Message> DataOutputStream.writeBatch(messages: Messages<T>) {
        messages.forEach {
            writeOne(it)
            logSend { it }
        }
    }

    private fun <T: Message> DataOutputStream.writeOne(t: T) {
        val dataInBytes: ByteArray = SerializationUtils.serialize(t)
        writeInt(dataInBytes.size)
        write(dataInBytes)
    }
}




