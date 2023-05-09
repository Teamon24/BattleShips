package org.home.style

import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Paint
import org.home.mvc.view.components.BattleButton
import org.home.style.AppStyles.Companion.buttonColorHex
import org.home.style.AppStyles.Companion.chosenCellColor
import tornadofx.InlineCss
import tornadofx.style

object CssUtils {
    fun BattleButton.hover() {
        style {
            val colorTransition1 = Paint.valueOf(buttonColorHex) as Color to chosenCellColor
            val colorTransition2 = BLACK to WHITE

            val cssPropertyTransform1: InlineCss.(Color) -> Unit = { backgroundColor += it }
            val cssPropertyTransform2: InlineCss.(Color) -> Unit = { textFill = it }

            this@hover.hoverTransition = HoverTransition(
                region = this@hover,
                time = 50.0,
                hoversInfo = listOf(
                    colorTransition1 to cssPropertyTransform1,
                    colorTransition2 to cssPropertyTransform2).toMap()
            )
        }
    }
}

