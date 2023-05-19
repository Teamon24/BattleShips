package org.home.style

import home.extensions.AnysExtensions.invoke
import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.CssRule
import tornadofx.CssSelectionBlock
import tornadofx.MultiValue
import tornadofx.Stylesheet.Companion.hover
import tornadofx.Stylesheet.Companion.selected
import tornadofx.box
import tornadofx.px

object CssUtils {
    fun AppStyles.height(dimension: LinearUnits): CssSelectionBlock.() -> Unit = { height(dimension) }
    fun AppStyles.selected(color: Color, text: Color? = null): CssSelectionBlock.() -> Unit = { selected(color, text) }
    fun AppStyles.hover(color: Color, text: Color? = null): CssSelectionBlock.() -> Unit = { hover(color, text) }
    fun AppStyles.size(dimension: LinearUnits): CssSelectionBlock.() -> Unit = { size(dimension) }
    fun AppStyles.prefWidth(width: LinearUnits): CssSelectionBlock.() -> Unit = { prefWidth = width }
    fun AppStyles.prefSize(width: LinearUnits, height: LinearUnits = width): CssSelectionBlock.() -> Unit {
        return {
            prefWidth = width
            prefHeight = height
        }
    }

    fun AppStyles.border(width: LinearUnits): CssSelectionBlock.() -> Unit = {
        borderColor += box(Color.BLACK)
        borderWidth += box(width)
    }

    inline val AppStyles.noBorder: CssSelectionBlock.() -> Unit get() = { borderWidth += box(0.px) }

    inline val AppStyles.square: CssSelectionBlock.() -> Unit
        get() {
            return {
                focusColor = Color.TRANSPARENT
                faintFocusColor = Color.TRANSPARENT
                backgroundRadius += box(0.px)
            }
        }

    fun radius(dimension: LinearUnits): CssSelectionBlock.() -> Unit = { backgroundRadius += box(dimension) }
    fun gridMargin(dimension: LinearUnits): CssSelectionBlock.() -> Unit = { gridMargin(dimension) }
    fun margin(dimension: LinearUnits): CssSelectionBlock.() -> Unit = { margin(dimension) }
    fun padding(dimension: LinearUnits): CssSelectionBlock.() -> Unit = { padding = box(dimension) }
    fun background(color: Color): CssSelectionBlock.() -> Unit = { background(color) }
    fun text(color: Color): CssSelectionBlock.() -> Unit = { text(color) }

    inline val Color.background: CssSelectionBlock.() -> Unit get() = { backgroundColor += this@background }
    inline val AppStyles.center: CssSelectionBlock.() -> Unit
        get() {
            return { alignment = Pos.CENTER }
        }

    inline val AppStyles.fillParent: CssSelectionBlock.() -> Unit
        get() {
            return {
                maxWidth = Short.MAX_VALUE.px
                maxHeight = Short.MAX_VALUE.px
            }
        }

    fun CssSelectionBlock.size(dimension: LinearUnits): CssSelectionBlock {
        minWidth = dimension
        minHeight = minWidth
        return this
    }

    fun CssSelectionBlock.text(color: Color) {
        textFill = color
    }

    fun CssSelectionBlock.gridMargin(dimension: LinearUnits) {
        hgap = dimension
        vgap = hgap
    }

    fun CssSelectionBlock.background(color: Color) {
        backgroundColor += color
    }

    fun CssSelectionBlock.selected(color: Color, text: Color? = null) {
        and(selected) {
            backgroundColor += color
            text?.also { textFill = it }
        }
    }

    fun CssSelectionBlock.hover(color: Color, text: Color? = null) {
        and(hover) {
            backgroundColor += color
            text?.also { textFill = it }
        }
    }


    fun CssSelectionBlock.height(dimension: LinearUnits) {
        prefHeight = dimension
    }

    fun CssSelectionBlock.margin(px: LinearUnits) {
        padding = box(px)
        backgroundInsets = MultiValue(arrayOf(box(px / 2)))
    }
}