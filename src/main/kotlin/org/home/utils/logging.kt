package org.home.utils

import javafx.event.Event
import kotlinx.coroutines.runBlocking
import org.home.ApplicationProperties
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.coord
import org.home.utils.functions.isNotUnit
import tornadofx.FXEvent
import tornadofx.UIComponent
import tornadofx.View
import java.net.Socket
import kotlin.reflect.KClass

fun threadLog(any: Any) = "[${Thread.currentThread().name}]: $any"

fun threadPrintln(any: Any) = println(threadLog(any))

fun threadPrint(any: Any) = print(threadLog(any))

fun threadPrintln(message: String) = println(threadLog(message))

fun logging(block: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.block()
    threadPrint(builder)
}

fun log(block: () -> Any) { threadPrintln(block()) }

fun logTitle(title: String, block: () -> Any) {
    val titlePart = buildString { repeat(7) { append('-') } }
    runBlocking {
        threadPrintln("$titlePart $title $titlePart")
        threadPrintln(block())
        threadPrintln("$titlePart $titlePart $titlePart")
    }
}


@JvmName("logEvent")
inline fun View.log(fxEvent: FXEvent, block: () -> Any = {}) {
    val title = "<<< ${this::class.simpleName}: ${fxEvent::class.simpleName} >>>"

    threadPrintln(title)
    val result = block()
    result.isNotUnit {
        log { result }
    }
    threadPrintln(title)
}

@JvmName("logEachEvent")
inline fun <E> View.logEach(fxEvent: FXEvent, collection: Collection<E>, block: (E) -> Any = {}) {
    val title = "<<< ${this::class.simpleName}: ${fxEvent::class.simpleName} >>>"

    threadPrintln(title)
    collection.forEach {
        val result = block(it)
        result.isNotUnit {
            log { result }
        }
    }
    threadPrintln(title)
}


fun logCom(client: String, block: () -> Any) {
    val title = "-------- ($client) #sendAndReceive --------"
    threadPrintln(title)
    block()
    threadPrintln(title)
    println()
}

fun logThreadClientWaiting(
    clients: Map<Socket, String>,
    client: Socket,
) {
    log {
        "waiting for ${
            clients[client]!!
                .ifEmpty { "connection" }
                .apply { ifNoEmpty { Thread.currentThread().name = "$this listener" } }
        } "
    }
}

fun logTransit(model: BattleModel, transit: String, from: UIComponent, to: KClass<out UIComponent>) {
    logging {
        ln("$transit: ${from.name} to ${to.name}")
        ln("model: (${model.width.value}, ${model.height.value})")
    }
}

infix fun StringBuilder.ln(s: Any): StringBuilder = append(s).append("\n")
infix fun StringBuilder.add(s: Any): StringBuilder = append(s)

fun logTransit(model: BattleModel, transit: String, from: UIComponent, to: UIComponent) {
    logging {
        ln("$transit: ${from::class.simpleName} to ${to::class.simpleName}")
        ln("model: (${model.width.value}, ${model.height.value})")
    }
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

fun log(battleModel: BattleModel) {
    logging {
        battleModel.battleShipsTypes.forEach { entry -> add(entry) }
        ln(battleModel.width.value)
        ln(battleModel.height.value)
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

private fun String.ifNoEmpty(function: String.() -> Unit) {
    if (this.isNotEmpty()) {
        this.function()
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
