package org.home.mvc.view.fleet

import javafx.event.Event
import javafx.event.EventType
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import org.home.mvc.model.BattleModel

fun <T : MouseEvent> FleetCell.leftClickHandler(eventType: EventType<T>, handle: FleetCell.() -> Unit) {
    this.addEventHandler(eventType) {
        if (it.isPrimary()) {
            it.log()
            this.handle()
        }
    }
}

fun <T : MouseEvent> FleetCell.rightClickHandler(eventType: EventType<T>, handle: FleetCell.() -> Unit) {
    this.addEventHandler(eventType) {
        if (it.isSecondary()) {
            it.log()
            this.handle()
        }
    }
}

fun coord(it: Event) = (it.source as FleetCell).coord

fun MouseEvent.isPrimary() = this.button == MouseButton.PRIMARY
fun MouseEvent.isSecondary() = this.button == MouseButton.SECONDARY

fun <T : Event> T.log() {
    buildString {
        append(source.javaClass.simpleName)
        if (source is FleetCell) {
            val coord = coord(this@log)
            append("[${coord.first}, ${coord.second}]")
        }
        append(": $eventType")
        println(this)
    }
}

fun log(battleModel: BattleModel) {
    battleModel.battleShipsTypes.forEach { entry -> println(entry) }
    println(battleModel.fleetGridWidth.value)
    println(battleModel.fleetGridHeight.value)
}
