package org.home.mvc.view

import javafx.geometry.Pos
import javafx.stage.Modality.APPLICATION_MODAL
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.style.AppStyles
import tornadofx.UIComponent
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.label

fun openAlertWindow(message: () -> String) = window(message).openWindow(modality = APPLICATION_MODAL)
fun openMessageWindow(message: () -> String) = window(message).openWindow(modality = APPLICATION_MODAL)

private fun window(message: () -> String) = object : UIComponent() {

    override val root = centerGrid {
        cell(0, 0) {
            label(message()) {
                addClass(AppStyles.errorLabel)
                alignment = Pos.CENTER
            }
        }
        cell(1, 0) { button("ok") { action { close() } } }
    }

    init {
        root.focusedProperty().addListener { _, _, newValue ->
            if (newValue == false) {
                close()
            }
        }
    }
}