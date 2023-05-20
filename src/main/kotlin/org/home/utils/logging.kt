package org.home.utils

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.isNotUnit
import home.extensions.AnysExtensions.refClass
import home.extensions.AnysExtensions.refNumber
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.event.Event
import org.home.mvc.model.BattleViewModel
import org.home.mvc.view.fleet.FleetCell
import org.home.net.server.Message
import org.home.net.server.MultiServer
import org.home.net.server.MultiServerThread
import org.home.utils.extensions.StringBuildersExtensions.LogBuilder
import org.home.utils.extensions.StringBuildersExtensions.add
import org.home.utils.extensions.StringBuildersExtensions.ln
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.View
import java.net.Socket
import kotlin.concurrent.thread

const val N = 40
const val COM_SIGN = "-"
const val LEFT_ARROW = "<|"
const val RIGHT_ARROW = "|>"
const val UI_EVENT_SIGN = "+"

val comString = COM_SIGN.repeat(N)

fun threadLog(any: Any?) = "[${Thread.currentThread().name}]: $any"
fun threadLog() = "[${Thread.currentThread().name}]"

fun threadPrintln(any: Any?) = println(threadLog(any))
fun threadPrintln() = println(threadLog())

fun threadPrintln(message: String) = println(threadLog(message))

fun threadErrorLn(any: Any?) = System.err.println(threadLog(any))

inline fun threadErrorLn(build: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.build()
    println(threadLog(builder))
}

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

val Component.componentName get() = "$refClass-[$refNumber]"

fun logError(throwable: Throwable, stackTrace: Boolean = false, body: () -> Any = {}) {
    val dot = "."
    val dots = dot.repeat(5)
    val ttl = listOf(dots, throwable.className, dots)

    val title = ttl.joinToString(" ")
    threadErrorLn {
        ln(dot.repeat(title.length))
        ln(title)
        ln(dot.repeat(title.length))
        ln(
            if (stackTrace) {
                throwable.stackTraceToString()
            } else {
                throwable.message
            }
        )
        body().isNotUnit { ln(it.toString()) }
        ln(dot.repeat(ttl.sumOf { it.length } + ttl.size - 1))
        ln()
    }
}

inline fun BattleViewModel.log(disabled: Boolean = false, block: BattleViewModel.() -> Any) {
    if (!disabled) {
        threadPrintln("::: MODEL[${refNumber}] ::: " + block())
    }
}

@JvmName("logEvent")
fun View.logEvent(fxEvent: FXEvent, modelView: BattleViewModel, body: () -> Any = {}) {
    val title = "${this::class.simpleName}[${modelView.getCurrentPlayer()}] <- $fxEvent"
    threadPrintln {
        ln()
        ln(line(title.length))
        ln(title)
        body().isNotUnit {
            ln(it.toString())
        }
        ln(line(title.length))
        ln()
    }
}

inline fun logTitle(titleContent: String = "", disabled: Boolean = false, block: () -> Any = {}) {
    if (!disabled) {
        val titleSign = "="
        val dots = titleSign.repeat(5)
        val ttl = listOf(dots, titleContent, dots)

        val any = block()
        val title = titleContent.isNotEmpty() then ttl.joinToString(" ") or titleSign.repeat(any.toString().length)
        threadPrintln(title)
        any.isNotUnit {
            threadPrintln(it)
        }
        threadPrintln(title)
    }
}

fun line(length: Int) = UI_EVENT_SIGN.repeat(length)


inline fun logReceive(disabled: Boolean = false, crossinline block: () -> Any) {
    if (!disabled) {
        log { "$LEFT_ARROW$comString ${block()}" }
    }
}

inline fun <T> logReceive(socket: Socket, block: () -> T) {
    logComLogic(socket, { "$LEFT_ARROW$it" }, block)
}

fun <S : Socket> logReceive(socket: S, messages: Collection<Message>) {
    logReceive(socket) {
        messages.forEach { logReceive { it } }
    }
}

inline fun logSend(disabled: Boolean = false, crossinline block: () -> Any) {
    if (!disabled) {
        log { "$comString$RIGHT_ARROW ${block()}" }
    }
}

inline fun <T> logSend(socket: Socket, block: () -> T) {
    logComLogic(socket, { "$it$RIGHT_ARROW" }, block)
}

inline fun <T> logComLogic(socket: Socket, comLine: (String) -> String, block: () -> T) {
    val dashesNumber = N
    val uppercase = socket.toString().replace("null ", "").uppercase()
    val line = comLine(COM_SIGN.repeat(dashesNumber))
    val title = "$line $uppercase $line"
    val title2 = "$line ${COM_SIGN.repeat(uppercase.length)} $line"

    threadPrintln()
    threadPrintln(title2)
    threadPrintln(title)
    threadPrintln(title2)
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

fun MultiServer<*, *>.logMultiServerThreads(b: Boolean = true) {
    val lengthOfMax = threads.map { it.name }.maxBy { it.length }.length
    thread {
        while (b) {
            Thread.sleep(10_000)
            processor { logMultiServerThread(lengthOfMax) }
            receiver { logMultiServerThread(lengthOfMax) }
            connector { logMultiServerThread(lengthOfMax) }
        }
    }
}

private fun MultiServerThread<*, *>.logMultiServerThread(lengthOfMax: Int) {
    val indent = " ".repeat(lengthOfMax - name.length)
    threadPrintln("$name$indent: alive/interrupted: $isAlive/$isInterrupted")
}


private fun coord(it: Event) = (it.source as FleetCell).coord
