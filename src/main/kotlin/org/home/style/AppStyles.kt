package org.home.style

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.LIGHTSLATEGRAY
import javafx.scene.paint.Color.ORANGERED
import javafx.scene.paint.Color.RED
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Color.rgb
import org.home.utils.ColorUtils.color
import org.home.utils.ColorUtils.opacity
import org.home.utils.CssUtils.background
import org.home.utils.CssUtils.border
import org.home.utils.CssUtils.center
import org.home.utils.CssUtils.fillParent
import org.home.utils.CssUtils.gridMargin
import org.home.utils.CssUtils.height
import org.home.utils.CssUtils.hover
import org.home.utils.CssUtils.margin
import org.home.utils.CssUtils.noBorder
import org.home.utils.CssUtils.padding
import org.home.utils.CssUtils.radius
import org.home.utils.CssUtils.selected
import org.home.utils.CssUtils.size
import org.home.utils.CssUtils.square
import org.home.utils.CssUtils.text
import tornadofx.CssRule
import tornadofx.CssSelection
import tornadofx.CssSelectionBlock
import tornadofx.Dimension
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.loadFont
import tornadofx.mixin
import tornadofx.px
import java.net.URI

typealias LinearUnits = Dimension<Dimension.LinearUnits>

class AppStyles : Stylesheet() {

    companion object {
        private const val targetIconPath = "/icons/target-3699.svg"

        val titleCellColor = LIGHTSLATEGRAY
        val selectedColor = "#085191".color.opacity(0.7)
        val readyColor: Color = "408802FF".color
        val missCellColor = "A4A5A6FF".color

        val hitCellColor = ORANGERED
        val sunkCellColor = BLACK

        val readyCellColor: Color = readyColor.brighter()
        val readyTitleColor: Color = readyColor.darker()
        val hoveredReadyTitleColor = "D39D00FF".color

        val incorrectCellColor: Color = RED
        val shipBorderColor = RED
        val shipBorderCellColor = shipBorderColor.opacity(0.3)
        val shipBorderCellBorderColor = shipBorderColor.opacity(0.15)
        val initialAppColor = "E8E3E4FF".color
        val defeatedColor = "A93638F4".color
        val defeatedEmptyCellColor = defeatedColor.opacity(0.7)
        const val defeatedPlayerOpacity = 0.5
        val defeatedPlayerColor = defeatedColor.opacity(defeatedPlayerOpacity)

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

        const val fleetBorderWidth = 0.5

        val debugClass by cssclass()

        val gridMargin by cssclass()
        val animationGridMargin by cssclass()
        val shipsTypesInfoPane by cssclass()
        val centerGrid by cssclass()

        val shipTypeLabel by cssclass()
        val fleetLabel by cssclass()
        val errorLabel by cssclass()
        val currentPlayerLabel by cssclass()
        val defeatedPlayerLabel by cssclass()
        val readyPlayerLabel by cssclass()
        val fullShipNumberLabel by cssclass()
        val readyShipNumberLabel by cssclass()
        val defeatedShipNumberLabel by cssclass()
        val readyButton by cssclass()

        val fleetGrid by cssclass()

        val emptyCell by cssclass()
        val fleetCell by cssclass()
        val selectedCell by cssclass()
        val incorrectEmptyCell by cssclass()
        val shipBorderCell by cssclass()

        val titleCell by cssclass()
        val enemyCell by cssclass()
        val readyCell by cssclass()
        val readyTitleCell by cssclass()
        val readyShipTypeLabel by cssclass()
        val defeatedShipTypeLabel by cssclass()

        val hitCell by cssclass()
        val missCell by cssclass()
        val sunkCell by cssclass()

        val incorrectCell by cssclass()
        val defeatedEmptyCell by cssclass()
        val defeatedTitleCell by cssclass()

        val animationCell by cssclass()
        val enemyListCell by cssclass()
        val emptyListCell by cssclass()

        val defeatedListCell by cssclass()
        val readyListCell by cssclass()

    }

    init {
        debugClass {
            gridMargin(10.px)
            borderColor += box(BLACK)
            borderWidth += box(fleetBorderWidth.px)
        }


        //ENEMIES LIST VIEW
        odd              + background(WHITE)   + margin(1.px)
        even             + background(WHITE)   + margin(1.px)

        emptyListCell    + background(WHITE) + height(fleetCellSize)
        enemyListCell    + listCell(selectedColor, BLACK)
        defeatedListCell + listCell(defeatedColor, BLACK)
        readyListCell    + listCell(readyColor,    BLACK)


        //---- components style ------------------------------------------------------------
        listView {
            jetBrainFont()
            noBorder()
            focusTraversable = false
        }

        fleetGrid {
            focusTraversable = false
        }

        label     + jetBrainFont
        textField {
            square()
            focusTraversable = false
        }
        button    + background(initialAppColor) + jetBrainFont + fillParent + square

        form {
            center()
            fontSize = 15.px
        }

        //---- labels style ----------------------------------------------------------------
        errorLabel + padding(20.px)
        fleetLabel + center + jetBrainFont + cellSize

        defeatedPlayerLabel + text(WHITE) + margin(5.px) + background(defeatedPlayerColor)
        currentPlayerLabel  + text(WHITE) + margin(5.px) + background(selectedColor.opacity(defeatedPlayerOpacity))
        readyPlayerLabel    + text(WHITE) + margin(5.px) + background(readyColor.opacity(defeatedPlayerOpacity))

        shipTypeLabel {
            backgroundRadius += box(25.px, 25.px, 5.px, 5.px)
        }

        //---- panes style -----------------------------------------------------------------
        shipsTypesInfoPane + background(rgb(58, 132, 192, 0.35)) + margin(10.px)

        //---- buttons style ---------------------------------------------------------------
        readyButton + background(readyCellColor) + text(WHITE)

        //---- grids style -----------------------------------------------------------------
        centerGrid          + center
        fleetGrid           + border                    + center
        gridMargin          + gridMargin(marginValue)
        animationGridMargin + gridMargin(10.px)

        //---- fleet  cells style ----------------------------------------------------------
        fleetCell               + border
        emptyCell               + border   + hover(selectedColor)
        incorrectEmptyCell      + border   + hover(incorrectCellColor)

        selectedCell            + noBorder + background(selectedColor)      + text(WHITE)
        readyCell               + noBorder + background(readyCellColor)     + hover(hoveredReadyTitleColor)
        defeatedEmptyCell       + border   + background(defeatedEmptyCellColor)
        incorrectCell           + noBorder + background(incorrectCellColor)

        hitCell                 + noBorder + background(hitCellColor)
        sunkCell                + noBorder + background(sunkCellColor)
        missCell                + border   + background(missCellColor) + radius(fleetCellSize / 2)

        titleCell               + border   + background(titleCellColor)     + text(WHITE)
        readyTitleCell          + border   + background(readyTitleColor)    + text(WHITE)
        defeatedTitleCell       + border   + background(defeatedColor)      + text(WHITE)

        readyShipTypeLabel      + noBorder + background(readyTitleColor)    + text(WHITE)
        defeatedShipTypeLabel   + noBorder + background(defeatedColor)      + text(WHITE)

        fullShipNumberLabel     + noBorder + background(selectedColor)      + text(WHITE) + radius(fleetCellSize/2)
        readyShipNumberLabel    + noBorder + background(readyTitleColor)    + text(WHITE) + radius(fleetCellSize/2)
        defeatedShipNumberLabel + noBorder + background(defeatedColor)      + text(WHITE) + radius(fleetCellSize/2)

        animationCell           + noBorder


        (shipBorderCell + background(shipBorderCellColor)) {
            borderColor += box(shipBorderCellBorderColor)
            borderWidth += box(fleetBorderWidth.px)
        }

        enemyCell {
            border()
            and(hover) {
                cursor = Cursor.CROSSHAIR
                backgroundColor += rgb(231, 64, 67, 0.5)
                backgroundImage += URI(targetIconPath)
            }
        }
    }

    private fun listCell(color: Color, text: Color): CssSelectionBlock.() -> Unit {
        return {
            text(text)
            background(color.opacity(0.1))
            height(fleetCellSize)
            selected(color, WHITE)
        }
    }

    private inline val AppStyles.cellSize: CssSelectionBlock.() -> Unit get() { return size(fleetCellSize) }
    private inline val AppStyles.border: CssSelectionBlock.() -> Unit get() { return border(fleetBorderWidth.px) }
    private inline val AppStyles.jetBrainFont: CssSelectionBlock.() -> Unit
        get() {
            return {
                +jetBrainsMonoLightFont
                +small
            }
        }

    inline operator fun CssRule.plus(crossinline block: CssSelectionBlock.() -> Unit): CssRule {
        this { block() }
        return this
    }

    operator fun (CssSelectionBlock.() -> Unit).plus(selection: CssSelection) {
        selection.block.this()
    }
}




