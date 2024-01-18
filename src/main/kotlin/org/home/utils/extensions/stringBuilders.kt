package org.home.utils.extensions

import home.extensions.StringsExtensions.isNotEmpty
import org.home.utils.threadLog

object StringBuildersExtensions {

    fun StringBuilder.ln(s: String? = ""): StringBuilder = apply {
        s?.isNotEmpty { append(s) }
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