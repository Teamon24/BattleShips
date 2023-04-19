package org.home.utils

import javafx.event.Event
import org.home.ApplicationProperties
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.coord
import org.home.net.socket.ex.sendSign
import tornadofx.UIComponent
import kotlin.reflect.KClass

fun logger(block: StringBuilder.() -> Unit) {
    val builder = StringBuilder()
    builder.block()
    threadPrint(builder)
}

fun log(block: () -> Any) {
    threadPrintln(block())
}

fun logCom(client: String, block: () -> Any) {
    threadPrintln("->>>>>>> ($client) #sendAndReceive ->>>>>>>")
    block()
    threadPrintln("<<<<<<<- ($client) #sendAndReceive <<<<<<<-")
    println()
}

fun logTransit(model: BattleModel, transit: String, from: UIComponent, to: KClass<out UIComponent>) {
    logger {
        ln("$transit: ${from.name} to ${to.name}")
        ln("model: (${model.width.value}, ${model.height.value})")
    }
}

infix fun StringBuilder.ln(s: Any) {
    append(s).append("\n")
}

infix fun StringBuilder.add(s: Any) {
    append(s)
}

fun logTransit(model: BattleModel, transit: String, from: UIComponent, to: UIComponent) {
    logger {
        ln("$transit: ${from::class.simpleName} to ${to::class.simpleName}")
        ln("model: (${model.width.value}, ${model.height.value})")
    }
}

fun UIComponent.logInject(vararg injected: KClass<*>) {
    logger {
        ln("${this@logInject.name} (injected): " + injected.joinToString(",") { it.name })
    }
}

fun UIComponent.logInject(vararg injected: Any) {
        logger {
            ln("${this@logInject.name} (injected): " + injected.joinToString(",") { it.name })
        }
}

fun log(battleModel: BattleModel) {
    logger {
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

        logger {
            ln(this)
        }
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