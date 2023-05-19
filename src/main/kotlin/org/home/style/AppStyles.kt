package org.home.style

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.DARKCYAN
import javafx.scene.paint.Color.DARKGREEN
import javafx.scene.paint.Color.DARKRED
import javafx.scene.paint.Color.GREY
import javafx.scene.paint.Color.LIGHTSLATEGRAY
import javafx.scene.paint.Color.ORANGERED
import javafx.scene.paint.Color.RED
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Color.rgb
import org.home.style.ColorUtils.color
import org.home.style.ColorUtils.opacity
import org.home.style.CssUtils.background
import org.home.style.CssUtils.border
import org.home.style.CssUtils.center
import org.home.style.CssUtils.fillParent
import org.home.style.CssUtils.gridMargin
import org.home.style.CssUtils.hover
import org.home.style.CssUtils.jetBrainFont
import org.home.style.CssUtils.margin
import org.home.style.CssUtils.noBorder
import org.home.style.CssUtils.padding
import org.home.style.CssUtils.radius
import org.home.style.CssUtils.selected
import org.home.style.CssUtils.size
import org.home.style.CssUtils.square
import org.home.style.CssUtils.text
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

        val selectedColor = "#085191".color.opacity(0.7)
        val missCellColor = "A4A5A6FF".color
        val hitCellColor = ORANGERED
        val titleCellColor = LIGHTSLATEGRAY
        val sunkCellColor = BLACK

        val readyColor: Color = "408802FF".color
        val readyCellColor: Color = readyColor.brighter()
        val readyTitleColor: Color = readyColor.darker()
        val hoveredReadyTitleColor = "D39D00FF".color

        val incorrectCellColor: Color = RED
        val shipBorderColor = RED
        val shipBorderCellColor = shipBorderColor.opacity(0.3)
        val shipBorderCellBorderColor = shipBorderColor.opacity(0.15)
        val initialAppColor = "E8E3E4FF".color
        val defeatedColor = "A93638F4".color
        val defeatedCellColor = defeatedColor.opacity(0.5)

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

        val hitCell by cssclass()
        val missCell by cssclass()
        val sunkCell by cssclass()
        val currentPlayerCell by cssclass()

        val incorrectCell by cssclass()
        val defeatedEmptyCell by cssclass()
        val defeatedTitleCell by cssclass()

        val animationCell by cssclass()
        val enemyListCell by cssclass()
        val defeatedListCell by cssclass()
        val readyListCell by cssclass()

//        #players-list-view .list-cell:filled:selected:focused,
//
//        #players-list-view .list-cell:filled:selected {
//            -fx-background-color:  #0A65BF;
//            -fx-text-fill: white;
//        }
//
//        #players-list-view .list-cell:even { /* <=== changed to even */
//            -fx-background-color: white;
//        }
//
    }

    init {
        debugClass {
            gridMargin(10.px)
            borderColor += box(BLACK)
            borderWidth += box(fleetBorderWidth.px)
        }

        odd              + background(WHITE)   + margin(1.px)
        even             + background(WHITE)   + margin(1.px)
        enemyListCell    + text(BLACK)         + selected(selectedColor, WHITE)
        defeatedListCell + text(defeatedColor) + selected(defeatedColor, WHITE)
        readyListCell    + text(readyColor)    + selected(readyColor, WHITE)

        //---- components style ------------------------------------------------------------
        label     + jetBrainFont
        textField + square
        button    + background(initialAppColor) + jetBrainFont + fillParent + square

        form {
            center()
            fontSize = 15.px
        }

        //---- labels style ----------------------------------------------------------------
        errorLabel + padding(20.px)
        fleetLabel + center + jetBrainFont + cellSize

        defeatedPlayerLabel + text(WHITE) + margin(5.px) + background(defeatedColor.opacity(0.5))
        currentPlayerLabel + text(WHITE) + margin(5.px) + background(selectedColor.opacity(0.5))
        readyPlayerLabel + text(WHITE) + margin(5.px) + background(readyColor.opacity(0.5))

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
        readyCell               + border   + background(readyCellColor)     + hover(hoveredReadyTitleColor)
        defeatedEmptyCell       + border   + background(defeatedCellColor)
        incorrectCell           + noBorder + background(incorrectCellColor)

        hitCell                 + border   + background(hitCellColor)
        missCell                + border   + radius(fleetCellSize / 2)
        sunkCell                + border   + background(sunkCellColor)

        titleCell               + border   + background(titleCellColor)     + text(WHITE)
        readyTitleCell          + border   + background(readyTitleColor)    + text(WHITE)
        defeatedTitleCell       + border   + background(defeatedColor)      + text(WHITE)
        readyShipTypeLabel      + noBorder + background(readyTitleColor)    + text(WHITE)

        fullShipNumberLabel     + noBorder + background(selectedColor)      + text(WHITE) + radius(fleetCellSize/2)
        readyShipNumberLabel    + noBorder + background(readyTitleColor)    + text(WHITE) + radius(fleetCellSize/2)
        defeatedShipNumberLabel + noBorder + background(defeatedColor)      + text(WHITE) + radius(fleetCellSize/2)
        animationCell           + noBorder


        (shipBorderCell + background(shipBorderCellColor)) {
            borderColor += box(shipBorderCellBorderColor)
            borderWidth += box(fleetBorderWidth.px)
        }

        currentPlayerCell {
            border()
            focusTraversable = false
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

    inline val AppStyles.cellSize: CssSelectionBlock.() -> Unit get() { return { size(fleetCellSize) } }

    inline operator fun CssRule.plus(crossinline block: CssSelectionBlock.() -> Unit): CssRule {
        this { block() }
        return this
    }

    operator fun (CssSelectionBlock.() -> Unit).plus(selection: CssSelection) {
        selection.block.this()
    }
}




