package org.home.style

import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.home.style.AppStyles.Companion.fleetBorderWidth
import tornadofx.CssSelectionBlock
import tornadofx.MultiValue
import tornadofx.Stylesheet.Companion.hover
import tornadofx.Stylesheet.Companion.selected
import tornadofx.box
import tornadofx.px

object CssUtils {

    fun AppStyles.prefWidth(width: LinearUnits): CssSelectionBlock.() -> Unit {
        return { prefWidth = width }
    }

    fun AppStyles.prefSize(width: LinearUnits, height: LinearUnits = width): CssSelectionBlock.() -> Unit {
        return {
            prefWidth = width
            prefHeight = height
        }
    }

    fun AppStyles.selected(color: Color, text: Color? = null): CssSelectionBlock.() -> Unit {
        return {
            and(selected) {
                backgroundColor += color
                text?.also { textFill = it }
            }
        }
    }

    fun AppStyles.hover(color: Color, text: Color? = null): CssSelectionBlock.() -> Unit {
        return {
            and(hover) {
                backgroundColor += color
                text?.also { textFill = it }
            }
        }
    }

    val AppStyles.border: CssSelectionBlock.() -> Unit
        get() { return {
            borderColor += box(Color.BLACK)
            borderWidth += box(fleetBorderWidth.px)
        }
        }

    inline val AppStyles.noBorder: CssSelectionBlock.() -> Unit
        get() { return { borderWidth += box(0.px) } }

    inline val AppStyles.jetBrainFont: CssSelectionBlock.() -> Unit
        get() { return {
            +AppStyles.jetBrainsMonoLightFont
            +AppStyles.small
        }
        }

    inline val AppStyles.square: CssSelectionBlock.() -> Unit
        get() { return {
            focusColor = Color.TRANSPARENT
            faintFocusColor = Color.TRANSPARENT
            backgroundRadius += box(0.px)
        }
        }

    fun radius(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return { backgroundRadius += box(dimension) }
    }

    fun gridMargin(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return { hgap = dimension
            vgap = hgap }
    }

    fun margin(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return { margin(dimension) }
    }

    fun padding(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return {
            padding = box(dimension)
        }
    }

    val Color.background: CssSelectionBlock.() -> Unit get() = { backgroundColor += this@background }
    fun background(color: Color): CssSelectionBlock.() -> Unit = { backgroundColor += color }

    fun text(color: Color): CssSelectionBlock.() -> Unit = { textFill = color }


    inline val AppStyles.center: CssSelectionBlock.() -> Unit get() { return { alignment = Pos.CENTER } }

    inline val AppStyles.fillParent: CssSelectionBlock.() -> Unit
        get() { return {
            maxWidth = Short.MAX_VALUE.px
            maxHeight = Short.MAX_VALUE.px
        }
        }

    fun CssSelectionBlock.size(dimension: LinearUnits): CssSelectionBlock {
        minWidth = dimension
        minHeight = minWidth
        return this
    }

    fun CssSelectionBlock.margin(px: LinearUnits) {
        padding = box(px)
        backgroundInsets = MultiValue(arrayOf(box(px / 2)))
    }




}