package org.home.mvc.view.component.button

import javafx.scene.control.Button
import org.home.mvc.ApplicationProperties.Companion.buttonHoverTransitionTime
import org.home.style.AppStyles.Companion.selectedColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.StyleUtils.textFillTransition
import org.home.style.Transition
import org.home.style.TransitionDSL.hovering
import org.home.style.TransitionDSL.transition
import tornadofx.style

class BattleButton(text: String) : Button(text) {
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
                transition(initialAppColor, selectedColor) { backgroundColor += it }
                textFillTransition()
            }
        }
    }
}
