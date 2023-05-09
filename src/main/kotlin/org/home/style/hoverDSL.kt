package org.home.style

import javafx.animation.FillTransition
import javafx.animation.Interpolator
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import org.home.style.AppStyles.Companion.buttonColor
import org.home.style.AppStyles.Companion.chosenCellColor
import org.home.utils.log
import tornadofx.InlineCss
import tornadofx.style



class HoverDTO(val inlineCss: InlineCss, val from: Color, val to: Color, val body: InlineCss.(Color) -> Unit)

class Hover(private val region: Region) {
    var time: Double = 0.0
        set(value) { field = value }
    val makes = mutableListOf<HoverDTO>()

    fun InlineCss.transition(fromColor: Color, toColor: Color, body: InlineCss.(Color) -> Unit): HoverDTO {
        return HoverDTO(this, fromColor, toColor, body).also { makes.add(it);
            log { "adding $it to ${this@Hover}" }
            println(makes) }
    }

    fun create() {
        val map = makes.map { dto ->

        }
    }
}

fun Region.hover(init: Hover.() -> Unit) {
    return Hover(this).apply { init() }.create()
}

fun Region.test() {
    hover {
        time = 1000.0
        style {
            transition(BLACK, WHITE) {
                log { "changing textFill to $it" }
                textFill = it
            }
            transition(buttonColor as Color, chosenCellColor) {
                log { "changing backgroundColor to $it" }
                backgroundColor += it
            }
        }
    }
}

fun Button.hover() {
    val rect = Rectangle()
    rect.fill = buttonColor

    val duration = 50.0
    val transition = fillTransition(rect, duration, chosenCellColor, buttonColor as Color)
    transition.isAutoReverse = false
    transition.interpolator = curve {
        this.background = backgroundFill(rect.fill)
        this.textFill = Color.WHITE
    }

    setOnMouseEntered {
        transition.play()
    }

    setOnMouseExited {
        transition.stop()
        background = backgroundFill(chosenCellColor)
        textFill = Color.BLACK
    }
}

fun <T: Node> T.curve(function: T.() -> Unit): Interpolator {
    return object : Interpolator() {
        override fun curve(t: Double): Double {
            function()
            return t
        }
    }
}

private fun fillTransition(rect: Rectangle, duration: Double, toColor: Color, fromColor: Color): FillTransition {
    val tr = FillTransition()
    tr.shape = rect
    tr.duration = Duration.millis(duration)
    tr.fromValue = fromColor
    tr.toValue = toColor
    return tr
}
private fun backgroundFill(paint: Paint) =
    Background(BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY))