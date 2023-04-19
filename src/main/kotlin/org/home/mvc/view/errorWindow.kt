package org.home.mvc.view

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.stage.Modality.APPLICATION_MODAL
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.style.AppStyles
import tornadofx.UIComponent
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.label

fun openErrorWindow(message: () -> String) = window(message).openWindow(modality = APPLICATION_MODAL)
fun openWindow(message: () -> String) = window(message).openWindow(modality = APPLICATION_MODAL)

private fun window(message: () -> String) = object : UIComponent() {

    val view = this
    val EventTarget.label: Label
        get() = label(message()) {
            addClass(AppStyles.errorLabel)
            alignment = Pos.CENTER
        }
    override val root = centerGrid {
        cell(0, 0) { label }
        cell(1, 0) { button("ok") { action { view.close() } } }
    }
}