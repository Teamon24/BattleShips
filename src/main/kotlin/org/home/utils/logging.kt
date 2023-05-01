package org.home.utils

import javafx.event.Event
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.coord
import org.home.net.message.Message
import org.home.utils.extensions.AnysExtensions.isNotUnit
import org.home.utils.extensions.LogBuilder
import org.home.utils.extensions.add
import org.home.utils.extensions.className
import org.home.utils.extensions.ln
import tornadofx.FXEvent
import tornadofx.View
import java.net.Socket

const val N = 40
const val COM_SIGN = "-"
const val UI_EVENT_SIGN = "+"

val comString = COM_SIGN.repeat(N)

fun threadLog(any: Any?) = "[${Thread.currentThread().name}]: $any"
fun threadLog() = "[${Thread.currentThread().name}]"

fun threadPrintln(any: Any?) = println(threadLog(any))
fun threadPrintln() = println(threadLog())

fun threadPrintln(message: String) = println(threadLog(message))

inline fun threadPrintln(build: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.build()
    println(threadLog(builder))
}

inline fun logging(block: LogBuilder.() -> Unit) {
    val builder = LogBuilder()
    builder.block()
    println(builder)
}


inline fun <T> Collection<T>.logEach(block: (T) -> Any) {
    forEach {
        threadPrintln(block(it))
    }
}


inline fun log(disabled: Boolean = false, block: () -> Any?) {
    if (!disabled) {
        threadPrintln(block())
    }
}

fun logError(throwable: Throwable, stackTrace: Boolean = false) {
    val dot = "."
    val dots = dot.repeat(5)
    val ttl = listOf(dots, "handled", throwable.className, dots)

    val title = ttl.joinToString(" ")
    threadPrintln()
    threadPrintln(title)
    threadPrintln(if (stackTrace) { throwable.stackTraceToString() } else {throwable.message} )
    threadPrintln(dot.repeat(ttl.sumOf { it.length } + ttl.size - 1))
    threadPrintln()
}

inline fun BattleModel.log(disabled: Boolean = false, block: BattleModel.() -> Any) {
    if (!disabled) {
        threadPrintln("::: MODEL ::: " + block())
    }
}

@JvmName("logEvent")
fun View.logEvent(fxEvent: FXEvent, model: BattleModel, body: () -> Any = {}) {
    val title = "${this::class.simpleName}[${model.currentPlayer}] <- $fxEvent"
    threadPrintln { line(title.length) }
    threadPrintln { add(title) }
    body().isNotUnit { threadPrintln(it) }
    threadPrintln { line(title.length) }
}

inline fun logTitle(titleContent: String, disabled: Boolean = false, block: () -> Any) {
    if (!disabled) {
        val dot = "="
        val dots = dot.repeat(5)
        val ttl = listOf(dots, titleContent, dots)

        val title = ttl.joinToString(" ")
        threadPrintln(title)
        threadPrintln(block())
        threadPrintln(title)
    }
}

fun StringBuilder.line(length: Int) = repeat(length) { append(UI_EVENT_SIGN) }

inline fun logReceive(disabled: Boolean = false, crossinline block: () -> Any) {
    if (!disabled) {
        log { "<$comString ${block()}" }
    }
}

inline fun logSend(disabled: Boolean = false, crossinline block: () -> Any) {
    if (!disabled) {
        log { "$comString> ${block()}" }
    }
}

inline fun <T> logReceive(socket: Socket, block: () -> T) = logComLogic(socket, { "<$it" }, block)

fun <S: Socket> logReceive(socket: S, messages: Collection<Message>) {
    logReceive(socket) {
        messages.forEach { logReceive { it } }
    }
}
inline fun <T> logSend(socket: Socket, block: () -> T) = logComLogic(socket, { "$it>" }, block)

inline fun <T> logComLogic(socket: Socket, comLine: (String) -> String, block: () -> T) {
    val dashesNumber = N
    val line = COM_SIGN.repeat(dashesNumber)
    val uppercase = socket.toString().uppercase()
    val left = comLine(line)
    val title = "$left $uppercase $left"
    val title2 = "$left ${COM_SIGN.repeat(uppercase.length)} $line"

    threadPrintln()
    threadPrintln(title)
    block()
    threadPrintln(title2)
    threadPrintln()
}

fun <T : Event> T.logCoordinate() {
    buildString {
        add(source.javaClass.simpleName)
        if (source is FleetCell) {
            val coord = coord(this@logCoordinate)
            add("${coord.first}, ${coord.second}")
        }

        add(": $eventType")

        logging {
            ln(this)
        }
    }
}

