package org.home.mvc.view.fleet

import javafx.event.Event
import javafx.event.EventType
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import org.home.utils.logCoordinate

fun <T : MouseEvent> FleetCell.leftClickHandler(eventType: EventType<T>, handle: FleetCell.() -> Unit) {
    this.addEventHandler(eventType) {
        if (it.isPrimary()) {
            it.logCoordinate()
            this.handle()
        }
    }
}

fun <T : MouseEvent> FleetCell.rightClickHandler(eventType: EventType<T>, handle: FleetCell.() -> Unit) {
    this.addEventHandler(eventType) {
        if (it.isSecondary()) {
            it.logCoordinate()
            this.handle()
        }
    }
}

fun coord(it: Event) = (it.source as FleetCell).coord

fun MouseEvent.isPrimary() = this.button == MouseButton.PRIMARY
fun MouseEvent.isSecondary() = this.button == MouseButton.SECONDARY




