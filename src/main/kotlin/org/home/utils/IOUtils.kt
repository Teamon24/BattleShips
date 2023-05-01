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
    fun InputStream.readBatch(): Collection<Message> {
        return DataInputStream(this).readBatch()
    }

    private fun DataInputStream.readBatch(): Collection<Message> {
        val messages = mutableListOf<Message>()
        val messagesInfo = readOne() as MessagesInfo
        messages.add(messagesInfo)
        messagesInfo.number.repeat {
            messages.add(readOne())
        }
        return messages
    }

    private fun DataInputStream.readOne(): Message {
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

    private fun DataOutputStream.writeOne(t: Message) {
        val dataInBytes: ByteArray = SerializationUtils.serialize(t)
        writeInt(dataInBytes.size)
        write(dataInBytes)
    }
}




