package org.home.mvc.view.battle

import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventTarget
import javafx.scene.control.TextField
import org.home.mvc.contoller.GameController
import tornadofx.style
import tornadofx.textfield

class SettingsFieldsController: GameController() {
    fun EventTarget.widthField() = intField(modelView.getWidth())
    fun EventTarget.heightField() = intField(modelView.getHeight())
    fun EventTarget.playersField() = intField(modelView.getPlayersNumber())

    private fun EventTarget.intField(prop: SimpleIntegerProperty): TextField {
        return textfield(prop) {
            style {
                focusTraversable = false
            }

            setOnScroll { event ->
                val op: (Int) -> Int = if (event.deltaY < 0) {
                    { it - 1 }
                } else {
                    { it + 1 }
                }

                text(op)
            }

            focusedProperty().addListener { _, _, _ ->
                if (!text.matches(Regex("\\d+"))) {
                    text = ""
                }
            }
        }
    }

    private fun TextField.text(op: (Int) -> Int) {
        op(text.toInt()).moreThan(1) {
            text = it
        }
    }

    private fun Int.moreThan(i: Int, function: (String) -> Unit) {
        if (this > i) { function(this.toString()) }
    }
}