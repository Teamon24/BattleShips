package org.home.utils

import javafx.event.Event
import org.home.ApplicationProperties
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.coord
import org.home.utils.extensions.add
import org.home.utils.extensions.ifNotEmpty
import org.home.utils.extensions.isNotUnit
import org.home.utils.extensions.ln
import tornadofx.FXEvent
import tornadofx.UIComponent
import tornadofx.View
import java.net.Socket
import kotlin.reflect.KClass



const val receiveSign = "<=="
const val sendSign = "==>"

fun threadLog(any: Any) = "[${Thread.currentThread().name}]: $any"

fun threadPrintln(any: Any) = println(threadLog(any))

fun threadPrint(any: Any) = print(threadLog(any))

fun threadPrintln(message: String) = println(threadLog(message))

fun threadPrintln(build: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.build()
    println(threadLog(builder))
}

fun logging(block: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.block()
    threadPrint(builder)
}

fun log(disabled: Boolean = false, block: () -> Any) {
    if (!disabled) {
        threadPrintln(block())
    }
}

fun logTitle(title: String, disabled: Boolean = false, block: () -> Any) {
    if (!disabled) {
        threadPrintln(block())
    }
}

fun logReceive(disabled: Boolean = false, block: () -> Any) {
    if (!disabled) {
        log { "$receiveSign ${block()}" }
    }
}

fun logSend(disabled: Boolean = false, block: () -> Any) {
    if (!disabled) {
        log { "$sendSign ${block()}" }
    }
}

fun <T> Collection<T>.logEach(block: (T) -> Any) {
    forEach {
        threadPrintln(block(it))
    }
}

fun log(model: BattleModel) {
    log { "battle model" }
    log {
        model.battleShipsTypes.entries.joinToString(";")
    }
    log { model.width.value }
    log { model.height.value }
}

@JvmName("logEvent")
fun View.log(fxEvent: FXEvent, block: () -> Any = {}) {
    logView(fxEvent) {
        logResult(block)
    }
}

@JvmName("logEachEvent")
fun <E : Any> View.log(fxEvent: FXEvent, collection: Collection<E>, block: (E) -> Any = {}) {
    logView(fxEvent) {
        collection.forEach {
            logResult { block(it) }
        }
    }
}

fun View.logView(fxEvent: FXEvent, block: () -> Any = {}) {
    val (left, right) = "<<< " to " >>>"
    val title = "$left${this::class.simpleName}: ${fxEvent::class.simpleName}$right"

    threadPrintln(title)
    block()

    threadPrintln {
        append(left)
        repeat(title.length - left.length - right.length) { append("-") }
        append(right)
    }

    println()
}

fun logCom(client: String = "", block: () -> Any) {
    val title = "-------- ($client) --------"
    threadPrintln(title)
    block()
    threadPrintln(title)
    println()
}

fun logTransit(transit: String, from: UIComponent, to: UIKClass) {
    logTransit(transit, from, "-->", to)
}

fun logBackTransit(transit: String, from: UIComponent, to: UIKClass) {
    logTransit(transit, from,  "<--", to)
}


fun UIComponent.logInject(vararg injected: KClass<*>) {
    logging {
        ln("${this@logInject.name} (injected): " + injected.joinToString(",") { it.name })
    }
}

fun UIComponent.logInject(vararg injected: Any) {
        logging {
            ln("${this@logInject.name} (injected): " + injected.joinToString(",") { it.name })
        }
}

fun logThreadClientWaiting(
    clients: Map<Socket, String>,
    client: Socket,
) {
    log {
        "waiting for ${
            clients[client]!!
                .ifEmpty { "connection" }
                .apply {
                    ifNotEmpty { Thread.currentThread().name = "$this listener" } }
        } "
    }
}

fun <T : Event> T.log() {
    buildString {
        add(source.javaClass.simpleName)
        if (source is FleetCell) {
            val coord = coord(this@log)
            add("[${coord.first}, ${coord.second}]")
        }

        add(": $eventType")

        logging {
            ln(this)
        }
    }
}

private fun logResult(block: () -> Any) {
    val result = block()
    result.isNotUnit {
        log { result }
    }
}

private fun logTransit(
    transit: String,
    from: UIComponent,
    back: String,
    to: KClass<out UIComponent>,
) {
    logging {
        ln("$transit: ${from.name} $back ${to.name}")
    }
}


fun main() {
    println(ApplicationProperties::class.name)
    println(ApplicationProperties("application")::class.name)
    println(ApplicationProperties("application").name)
}

val KClass<*>.name: String
get() {
    var result = this.toString().replace("class", "").trim()
    result = result.substring(result.lastIndexOf('.') + 1)
    result = result.replace('$', '.')

    return result
}

val Any.name: String
    get() {
        var result = this.toString().replace("class", "").trim()
        result = result.substring(result.lastIndexOf('.') + 1)
        result = result.replace('$', '.')

        return result
    }
