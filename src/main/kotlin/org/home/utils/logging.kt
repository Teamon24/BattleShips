package org.home.utils

import javafx.event.Event
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetCell
import org.home.net.message.Message
import org.home.net.server.MultiServer
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.isNotUnit
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.extensions.AnysExtensions.refClass
import org.home.utils.extensions.AnysExtensions.refNumber
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.extensions.LogBuilder
import org.home.utils.extensions.add
import org.home.utils.extensions.className
import org.home.utils.extensions.ln
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.Scope
import tornadofx.View
import java.net.Socket
import kotlin.concurrent.thread

const val N = 40
const val COM_SIGN = "-"
const val leftArrow = "<|"
const val rightArrow = "|>"
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

inline fun logInject(target: Component, injection: Component) {
    log {
        "${target.componentName} <== ${injection.componentName}"
    }
}

inline fun logInject(targetName: String, injection: Component, scope: Scope) {
    log {
        "$targetName <== ${injection.componentName} with ${scope.name}"
    }
}


val Component.componentName get() = "$refClass-[$refNumber]"

fun logError(throwable: Throwable, stackTrace: Boolean = false) {
    val dot = "."
    val dots = dot.repeat(5)
    val ttl = listOf(dots, throwable.className, dots)

    val title = ttl.joinToString(" ")
    threadPrintln()
    threadPrintln(dot.repeat(title.length))
    threadPrintln(title)
    threadPrintln(dot.repeat(title.length))
    threadPrintln(if (stackTrace) { throwable.stackTraceToString() } else {throwable.message} )
    threadPrintln(dot.repeat(ttl.sumOf { it.length } + ttl.size - 1))
    threadPrintln()
}

inline fun BattleModel.log(disabled: Boolean = false, block: BattleModel.() -> Any) {
    if (!disabled) {
        threadPrintln("::: MODEL[${refNumber}] ::: " + block())
    }
}

@JvmName("logEvent")
fun View.logEvent(fxEvent: FXEvent, model: BattleModel, body: () -> Any = {}) {
    val title = "${this::class.simpleName}[${model.currentPlayer}] <- $fxEvent"
    threadPrintln { line(title.length) }
    threadPrintln { add(title) }
    body().isNotUnit {
        threadPrintln(it)
    }
    threadPrintln { line(title.length) }
}

inline fun logTitle(titleContent: String = "", disabled: Boolean = false, block: () -> Any = {}) {
    if (!disabled) {
        val titleSign = "="
        val dots = titleSign.repeat(5)
        val ttl = listOf(dots, titleContent, dots)

        val any = block()
        val title = titleContent.isNotEmpty() then  ttl.joinToString(" ") or titleSign.repeat(any.toString().length)
        threadPrintln(title)
        any.isNotUnit {
            threadPrintln(it)
        }
        threadPrintln(title)
    }
}

fun StringBuilder.line(length: Int) = repeat(length) { append(UI_EVENT_SIGN) }



inline fun logReceive(disabled: Boolean = false, crossinline block: () -> Any) {
    if (!disabled) {
        log { "$leftArrow$comString ${block()}" }
    }
}

inline fun logSend(disabled: Boolean = false, crossinline block: () -> Any) {
    if (!disabled) {
        log { "$comString$rightArrow ${block()}" }
    }
}

inline fun <T> logReceive(socket: Socket, block: () -> T) = logComLogic(socket, { "$leftArrow$it" }, block)

fun <S: Socket> logReceive(socket: S, messages: Collection<Message>) {
    logReceive(socket) {
        messages.forEach { logReceive { it } }
    }
}


inline fun <T> logSend(socket: Socket, block: () -> T) = logComLogic(socket, { "$it$rightArrow" }, block)

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

fun MultiServer<*, *>.logMultiServerThreads() {
    val lengthOfMax = threads.map { it.name }.maxBy { it.length }.length
    thread {
        while (true) {
            Thread.sleep(10_000)
            processor { logMultiServerThread(thread, lengthOfMax) }
            receiver { logMultiServerThread(thread, lengthOfMax) }
            accepter { logMultiServerThread(thread, lengthOfMax) }
        }
    }
}

fun logMultiServerThread(thread: Thread, lengthOfMax: Int) {
    thread.invoke {
        val indent = " ".repeat(lengthOfMax - name.length)
        threadPrintln("$name$indent: alive/interrupted: $isAlive/$isInterrupted")
    }
}


private fun coord(it: Event) = (it.source as FleetCell).coord

