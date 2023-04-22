package org.home.utils.extensions

import org.home.utils.threadPrintln

fun String.ifNotEmpty(function: String.() -> Unit) {
    if (this.isNotEmpty()) {
        this.function()
    }
}

infix fun StringBuilder.ln(s: Any): StringBuilder = append(s).append("\n")
infix fun StringBuilder.add(s: Any): StringBuilder = append(s)

class LogBuilder {
    val string = StringBuilder()
    override fun toString(): String {
        return string.toString().apply { string.clear() }
    }
}

infix fun LogBuilder.ln(s: Any): StringBuilder = string.append(threadPrintln(s))
infix fun LogBuilder.add(s: Any): StringBuilder = string.append(s)