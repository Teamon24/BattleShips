package org.home.mvc.view.components

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.yes
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import javafx.scene.paint.Color.*
import org.home.mvc.ApplicationProperties.Companion.buttonHoverTransitionTime
import org.home.mvc.ApplicationProperties.Companion.exitText
import org.home.mvc.ApplicationProperties.Companion.startButtonTransitionTime
import org.home.mvc.model.allAreReady
import org.home.mvc.view.AbstractGameView
import org.home.style.AppStyles.Companion.buttonColor
import org.home.style.AppStyles.Companion.chosenCellColor
import org.home.style.AppStyles.Companion.readyColor
import org.home.style.StyleUtils.textFillTransition
import org.home.style.Transition
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.hovering
import org.home.style.TransitionDSL.transition
import tornadofx.action
import tornadofx.attachTo
import tornadofx.style
import kotlin.system.exitProcess

inline fun EventTarget.exitButton(view: AbstractGameView) =
    battleButton(exitText) {
        action { view.exit() }
    }

inline fun EventTarget.exitButton() =
    battleButton(exitText) {
        action { exitProcess(0) }
    }

fun EventTarget.battleButton(text: String = "", graphic: Node? = null, op: Button.() -> Unit = {}) =
    BattleButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
    }

class BattleButton(text: String): Button(text) {
    private val currentNode = this@BattleButton
    var hoverTransition: Transition? = null

    fun disableHover() {
        hoverTransition?.disable()
        hoverTransition = null
    }

    init {
        style {
            hovering(currentNode) {
                millis = buttonHoverTransitionTime
                transition(buttonColor, chosenCellColor) { backgroundColor += it }
                textFillTransition()
            }
        }
    }
}

fun EventTarget.battleStartButton(text: String = "", graphic: Node? = null, op: BattleStartButton.() -> Unit = {}) =
    BattleStartButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
    }

class BattleStartButton(text: String): Button(text) {
    private val initialColor: Color = buttonColor
    private val thisButton = this@BattleStartButton

    fun updateStyle(view: AbstractGameView) {
        view {
            thisButton {
                if (applicationProperties.isServer) {
                    model.allAreReady
                        .yes { fillTransition { isDisable = false } }
                        .no {
                            isDisable.otherwise {
                                reverseFillTransition { isDisable = true }
                            }
                        }
                } else {
                    when (model.hasReady(currentPlayer)) {
                        true -> fillTransition()
                        else -> reverseFillTransition()
                    }
                }
            }
        }
    }

    private fun fillTransition(onFinish: () -> Unit = {}) {
        fillTransition(initialColor, readyColor, BLACK, WHITE, onFinish)
    }

    private fun reverseFillTransition(onFinish: () -> Unit = {}) {
        fillTransition(readyColor, initialColor, WHITE, BLACK, onFinish)
    }

    private fun fillTransition(from: Color, to: Color, textFrom: Color, textTo: Color, onFinish: () -> Unit) {
        style {
            filling(thisButton) {
                millis = startButtonTransitionTime
                transition(from, to) { backgroundColor += it }
                transition(textFrom, textTo) { textFill = it }
                onFinish(onFinish)
            }
        }
    }
}
