package org.home.utils

import org.apache.commons.lang3.SerializationUtils
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
        val messageBytes = `in`.readNBytes(size);
        val message = SerializationUtils.deserialize<T>(messageBytes)
        return message
    }
}