package org.home.style

import javafx.css.Styleable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Labeled
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import org.home.mvc.ApplicationProperties.Companion.fillingTransitionTime
import org.home.style.ColorUtils.color
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import org.home.utils.NodeUtils
import tornadofx.CssRule
import tornadofx.InlineCss
import tornadofx.addClass
import tornadofx.box
import tornadofx.getChildList
import tornadofx.px
import tornadofx.style

object StyleUtils {

    val Region.backgroundColor: Color get() = background?.fills?.get(0)?.fill?.color ?: WHITE
    val Labeled.textColor: Color get() = textFill?.color ?: WHITE

    fun Region.fillBackground(from: Color = backgroundColor, to: Color) =
        filling(from, to) { backgroundColor += it }

    fun Region.filling(from: Color, to: Color, cssProp: InlineCss.(Color) -> Unit) {
        style {
            filling(this@filling) {
                millis = fillingTransitionTime
                transition(from, to, cssProp)
            }
        }
    }

    fun Transition.textFillTransition() = transition(BLACK, WHITE) { textFill = it }

    fun Styleable.rightPadding(dimension: Int) {
        style {
            padding = box(0.px, dimension.px, 0.px, 0.px)
        }
    }

    fun Styleable.leftPadding(dimension: Int) {
        style {
            padding = box(0.px, 0.px, 0.px, dimension.px)
        }
    }
}