package org.home.style

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.DARKCYAN
import javafx.scene.paint.Color.DARKGREEN
import javafx.scene.paint.Color.DARKRED
import javafx.scene.paint.Color.GREY
import javafx.scene.paint.Color.LIGHTSLATEGRAY
import javafx.scene.paint.Color.MEDIUMSEAGREEN
import javafx.scene.paint.Color.ORANGERED
import javafx.scene.paint.Color.PINK
import javafx.scene.paint.Color.RED
import javafx.scene.paint.Color.TRANSPARENT
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Color.rgb
import javafx.scene.paint.Color.valueOf
import javafx.scene.paint.Paint
import org.home.style.ColorUtils.withOpacity
import org.home.utils.LinearUnits
import tornadofx.CssRule
import tornadofx.CssSelectionBlock
import tornadofx.MultiValue
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.loadFont
import tornadofx.mixin
import tornadofx.parallelTransition
import tornadofx.px
import java.net.URI

class AppStyles : Stylesheet() {

    class PlayerListViewColors(
        val turnColor: Color,
        val defeatedColor: Color,
        val readyColor: Color,
        val defaultColor: Color,
    )

    companion object {
        private const val targetIconPath = "/icons/target-3699.svg"

        val chosenCellColor: Color = valueOf("#085191").withOpacity(0.7)
        val readyPlayerCellColor: Color = MEDIUMSEAGREEN
        const val buttonColorHex = "EAE5E5FF"
        val buttonColor: Paint = Paint.valueOf(buttonColorHex)!!

        val currentPlayerListViewColors = PlayerListViewColors(DARKCYAN, DARKRED, DARKGREEN, GREY)
        val enemyListViewColors = PlayerListViewColors(chosenCellColor, RED, readyPlayerCellColor, BLACK)

        private const val fontName = "JetBrainsMono-Light.ttf"

        init {
            loadFont("/fonts/static/$fontName", 12)!!
        }

        val jetBrainsMonoLightFont = mixin {
            fontFamily =
                fontName
                    .dropLastWhile { it != '.' }
                    .split(" ")
                    .joinToString(" ")
        }

        val small = mixin { fontSize = 15.px }

        val fleetCellSize = 40.px
        val marginValue = 10.px

        private const val fleetBorderWidth = 0.5

        private val wrongCellColor = RED

        val form by cssclass()
        val fleetGrid by cssclass()

        val shipTypeLabel by cssclass()

        val emptyFleetCell by cssclass()
        val fleetLabel by cssclass()

        val missCell by cssclass()
        val hitCell by cssclass()

        val shipBorderCell by cssclass()

        val titleCell by cssclass()

        val incorrectFleetCell by cssclass()
        val fieldSize by cssclass()

        val chosenFleetCell by cssclass()
        val animationCell by cssclass()

        val gridMargin by cssclass()
        val animationGridMargin by cssclass()

        val debugClass by cssclass()
        val defeatedCell by cssclass()
        val defeatedTitleCell by cssclass()

        val centerGrid by cssclass()

        val shipsTypesInfoPane by cssclass()
        val currentPlayerCell by cssclass()
        val enemyCell by cssclass()
        val errorLabel by cssclass()
        val readyButton by cssclass()

        const val playersListView = "players-list-view"

    }

    init {
        errorLabel + padding(20.px)

        odd {
            margin(1.px)
            backgroundColor += WHITE
        }

        even {
            margin(1.px)
            backgroundColor += WHITE
        }

        label + jetBrainFont
        textField + square

        (button + jetBrainFont + fillParent + square) {
            backgroundColor += buttonColor

//            and(hover) {
//                backgroundColor += chosenCellColor.brighter(5)
//                textFill = WHITE
//            }
        }

        fleetLabel + jetBrainFont + cellSize + center
        centerGrid + center

        readyButton {
            backgroundColor += readyPlayerCellColor
            textFill = WHITE
        }

        chosenFleetCell {
            textFill = WHITE
            backgroundColor += chosenCellColor
            borderWidth += box(0.px)
        }

        animationCell {
            backgroundColor += chosenCellColor
            borderWidth += box(1.px)
            borderColor += box(WHITE)
        }

        (missCell + border) {
            backgroundColor += Paint.valueOf("A4A5A6FF")
        }

        (hitCell + border) {
            backgroundColor += ORANGERED
        }


        (titleCell + border) {
            textFill = WHITE
            backgroundColor += LIGHTSLATEGRAY
        }

        (emptyFleetCell + border) {
            and(hover) {
                backgroundColor += chosenCellColor
            }
        }

        (currentPlayerCell + border) { focusTraversable = false }

        (enemyCell + border) {
            and(hover) {
                cursor = Cursor.CROSSHAIR
                backgroundColor += rgb(231, 64, 67, 0.5)
                backgroundImage += URI(targetIconPath)
            }
        }

        (defeatedCell + border) {
            backgroundColor += PINK
        }

        defeatedTitleCell {
            backgroundColor += Paint.valueOf("A93638F4")
            textFill = WHITE
        }

        (shipsTypesInfoPane + margin(10.px)) {
            backgroundColor += rgb(58, 132, 192, 0.35)
        }

        shipTypeLabel {
            backgroundRadius += box(25.px, 25.px, 5.px, 5.px)
        }

        shipBorderCell {
            backgroundColor += rgb(255, 0, 0, 0.3)
            borderColor += box(rgb(255, 0, 0, 0.15))
            borderWidth += box(fleetBorderWidth.px)
        }

        incorrectFleetCell {
            backgroundColor += wrongCellColor
            borderWidth += box(0.px)
        }


        (debugClass + gridMargin(10.px)) {
            borderColor += box(BLACK)
            borderWidth += box(fleetBorderWidth.px)
        }

        form {
            val pref = 600.px
            alignment = Pos.CENTER
            prefHeight = pref
            prefWidth = pref
            fontSize = 15.px
        }

        fieldSize {
            maxWidth = 400.px
            maxHeight = 400.px
        }

        fleetGrid + center + border

        gridMargin + gridMargin(marginValue)

        animationGridMargin + gridMargin(10.px)
    }

    private val AppStyles.border: CssSelectionBlock.() -> Unit
        get() { return {
                borderColor += box(BLACK)
                borderWidth += box(fleetBorderWidth.px)
            }
        }

    private val AppStyles.jetBrainFont: CssSelectionBlock.() -> Unit
        get() { return {
                +jetBrainsMonoLightFont
                +small
            }
        }

    private val AppStyles.square: CssSelectionBlock.() -> Unit
        get() { return {
            focusColor = TRANSPARENT
            faintFocusColor = TRANSPARENT
            backgroundRadius += box(0.px)
        }
        }

    private fun gridMargin(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return { hgap = dimension
                 vgap = hgap }
        }

    private fun margin(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return { margin(dimension) }
    }

    private fun padding(dimension: LinearUnits): CssSelectionBlock.() -> Unit {
        return {
            padding = box(dimension)
        }
    }

    private val AppStyles.cellSize: CssSelectionBlock.() -> Unit get() { return { size(fleetCellSize) } }
    private val AppStyles.center: CssSelectionBlock.() -> Unit get() { return { alignment = Pos.CENTER } }

    private val AppStyles.fillParent: CssSelectionBlock.() -> Unit
        get() { return {
                maxWidth = Short.MAX_VALUE.px
                maxHeight = Short.MAX_VALUE.px
            }
        }

    private fun CssSelectionBlock.size(dimension: LinearUnits): CssSelectionBlock {
        minWidth = dimension
        minHeight = minWidth
        return this
    }

    private fun CssSelectionBlock.margin(px: LinearUnits) {
        padding = box(px)
        backgroundInsets = MultiValue(arrayOf(box(px / 2)))
    }

    operator fun CssRule.plus(block: CssSelectionBlock.() -> Unit): CssRule {
        this { block() }
        return this
    }
}

private fun Color.brighter(i: Int) = apply { repeat(i) { this.brighter() } }


