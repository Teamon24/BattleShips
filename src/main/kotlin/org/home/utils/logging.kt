package org.home.utils

import home.extensions.AnysExtensions.simpleName
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.anyOf
import home.extensions.AnysExtensions.isNotUnit
import home.extensions.AnysExtensions.name
import home.extensions.AnysExtensions.refClass
import home.extensions.AnysExtensions.refNumber
import home.extensions.BooleansExtensions.otherwise
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import javafx.event.Event
import org.home.mvc.model.BattleViewModel
import org.home.mvc.view.fleet.FleetCell
import org.home.net.server.Message
import org.home.net.server.MultiServer
import org.home.net.server.MultiServerThread
import org.home.utils.extensions.StringBuildersExtensions.LogBuilder
import org.home.utils.extensions.StringBuildersExtensions.add
import org.home.utils.extensions.StringBuildersExtensions.ln
import tornadofx.ChangeListener
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.View
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

const val N = 40
const val COM_SIGN = "-"
const val DEFAULT_TITLE_SYMBOL = "="
const val LEFT_ARROW  = "<<<"
const val RIGHT_ARROW = ">>>"
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

inline fun Unit.log(disabled: Boolean = false, block: () -> Any?) {
    if (!disabled) {
        threadPrintln(block())
    }
}

val Component.componentName get() = "$refClass-[$refNumber]"
val Any.componentName get() = "$refClass-[$refNumber]"

fun logError(throwable: Throwable, stackTrace: Boolean = false, body: () -> Any = {}) {
    val dot = "."
    val dots = dot.repeat(5)
    val ttl = listOf(dots, throwable.simpleName, dots)

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
        val result = block()
        result.anyOf(Unit)
            .otherwise { threadPrintln("${modelTitleContent()} $result") }
    }
}

fun BattleViewModel.modelTitleContent() = "::: MODEL[${refNumber}] :::"

@JvmName("logEvent")
fun View.logEvent(fxEvent: FXEvent, modelView: BattleViewModel, body: () -> Any = {}) {
    val title = "${this.simpleName}[${modelView.getCurrentPlayer()}] <- $fxEvent"
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

const val defaultTitleSymbolsNumber = 15

inline fun logTitle(titleContent: String = "",
                    disabled: Boolean = false,
                    titleSymbolsNumber: Int = defaultTitleSymbolsNumber,
                    titleSymbol: String = DEFAULT_TITLE_SYMBOL,
                    block: () -> Any = {},
) {
    if (!disabled) {
        val titleSide = titleSymbol.repeat(titleSymbolsNumber)
        val any = block()
        val title = titleContent.ifBlank { titleSymbol.repeat(any.toString().length) }
        val ttl = listOf(titleSide, title, titleSide)
        threadPrintln(ttl.joinToString(" "))
    }
}

inline fun logEmptyTitle(emptyTitleLength: Int,
                         disabled: Boolean = false,
                         block: () -> Any = {},
                         titleSymbol: String = DEFAULT_TITLE_SYMBOL,
                         titleSymbolsNumber: Int = defaultTitleSymbolsNumber
) {
    logTitle(titleSymbol.repeat(emptyTitleLength), disabled, titleSymbolsNumber, titleSymbol, block)
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
    val uppercase = socket.omitIfLocalhost()
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

fun <T : View> View.logTransit(replacement: T) {
    org.home.utils.log {
        "|////////////////////////////////////////////////| $componentName |/////| |> ${replacement.componentName}"
    }
}

inline fun Any.logInject(injection: Any, disabled: Boolean = false) {
    disabled.otherwise {
        log {
            "$componentName <== ${injection.componentName}"
        }
    }
}

inline fun Any.logInject(injection: Any, scope: Any, disabled: Boolean = false) {
    disabled.otherwise {
        log {
            "$componentName <== ${injection.componentName} with ${scope.name}"
        }
    }
}

fun SimpleIntegerProperty.logOnChange(name: String) = apply {
    addListener(ChangeListener { _, _, newValue -> log { "$name - $newValue" } })
}

fun SimpleStringProperty.logOnChange(name: String) = apply {
    addListener(ChangeListener { _, _, newValue -> log { "$name - $newValue" } })
}

fun <T> SimpleListProperty<T>.logOnChange(name: String) = apply {
    addListener(ListChangeListener { log { "$name - ${it.list}" } })
}

fun <T> SimpleSetProperty<T>.logOnChange(name: String) = apply {
    addListener(SetChangeListener { log { "$name - ${it.set}" } })
}

fun <K, V> SimpleMapProperty<K, V>.logOnChange(name: String) = apply {
    addListener(MapChangeListener { log { "$name - ${it.map}" } })
}


fun Socket.omitIfLocalhost(): String {
    return toString()
        .lowercase()
        .replace("socket", "CLIENT")
        .replace("127.0.0.1", "")
        .replace("addr=", "")
        .replace("localhost/", "")
        .replace("localhost", "")
        .replace(",port", "")
        .replace(",localport=", "/local=")
        .replace("socket", "")
        .replace("null ", "")
        .uppercase()
}

fun ServerSocket.omitIfLocalhost(): String {
    return toString()
        .lowercase()
        .replace("serversocket", "SERVER")
        .replace("127.0.0.1", "")
        .replace("addr=", "")
        .replace("localhost/", "")
        .replace("localhost", "")
        .replace(",port", "")
        .replace(",localport=", "/local=")
        .replace("null ", "")
        .uppercase()
}

fun MultiServer<*, *>.logServerStart() {
    logFrame(serverSocket().omitIfLocalhost(), 24) { title, i ->
        connector { logStart(title, i) }
        receiver { logStart(title, i) }
        processor { logStart(title, i) }
    }
}

fun logFrame(title: String, i: Int, symbol: String = DEFAULT_TITLE_SYMBOL, block: (String, Int) -> Unit) {
    title.also {
        logEmptyTitle(it.length, titleSymbolsNumber = i, titleSymbol = symbol)
        logTitle(it, titleSymbolsNumber = i, titleSymbol = symbol)
        logEmptyTitle(it.length, titleSymbolsNumber = i, titleSymbol = symbol)
        block(it, i)
        logEmptyTitle(it.length, titleSymbolsNumber = i, titleSymbol = symbol)
    }
}

fun BattleViewModel.logProps() {
    val titleContent = modelTitleContent()
    titleContent {
        logFrame(this, 10, symbol = "-") { _, _ ->
            Unit.log { "height          : ${getHeight().value}" }
            Unit.log { "width           : ${getWidth().value}" }
            Unit.log { "playersNumber   : ${getPlayersNumber().value}" }
            Unit.log { "players         : ${getPlayers().toMutableList()}" }
            Unit.log { "enemies         : ${getEnemies().toMutableList()}" }
            Unit.log { "ready           : ${getReadyPlayers().toMutableSet()}" }
            Unit.log { "fleetsReadiness : ${noPropertyFleetReadiness()}" }
            Unit.log { "battleIsStarted : ${battleIsStarted()}" }
            Unit.log { "battleIsEnded   : ${battleIsEnded()}" }
        }
    }
}

private fun MultiServerThread<out Message, out Socket>.logStart(title: String, i: Int) {
    val s = "STARTED"
    logTitle("${name}${" ".repeat(title.length - name.length - s.length)}$s", titleSymbolsNumber = i)
}

private fun coord(it: Event) = (it.source as FleetCell).coord
