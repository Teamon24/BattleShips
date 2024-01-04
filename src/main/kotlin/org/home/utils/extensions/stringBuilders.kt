package org.home.utils.extensions

import home.extensions.StringsExtensions.ifNotEmpty
import org.home.utils.threadLog

object StringBuildersExtensions {

    fun StringBuilder.ln(s: String? = ""): StringBuilder = apply {
        s?.ifNotEmpty { append(s) }
        append("\n")
    }
    infix fun StringBuilder.add(s: Any): StringBuilder = append(s)

    class LogBuilder {
        val builder = StringBuilder()
        override fun toString(): String {
            return builder.toString().apply { builder.clear() }
        }
    }

    infix fun LogBuilder.ln(s: Any): StringBuilder = builder.append(threadLog(s)).ln()
}