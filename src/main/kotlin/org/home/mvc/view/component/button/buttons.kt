package org.home.mvc.view.component.button

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Button
import org.home.app.ApplicationProperties.Companion.exitText
import tornadofx.action
import tornadofx.attachTo
import kotlin.system.exitProcess

inline fun EventTarget.exitButton() = battleButton(exitText) { action { exitProcess(0) } }

fun EventTarget.battleButton(text: String = "", graphic: Node? = null, op: Button.() -> Unit = {}) =
    BattleButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
    }


