package org.home.style

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
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Color.rgb
import org.home.style.ColorUtils.color
import org.home.style.ColorUtils.withOpacity
import org.home.style.CssUtils.background
import org.home.style.CssUtils.border
import org.home.style.CssUtils.cellSize
import org.home.style.CssUtils.center
import org.home.style.CssUtils.fillParent
import org.home.style.CssUtils.gridMargin
import org.home.style.CssUtils.jetBrainFont
import org.home.style.CssUtils.margin
import org.home.style.CssUtils.noBorder
import org.home.style.CssUtils.padding
import org.home.style.CssUtils.radius
import org.home.style.CssUtils.square
import org.home.style.CssUtils.text
import tornadofx.CssRule
import tornadofx.CssSelection
import tornadofx.CssSelectionBlock
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.loadFont
import tornadofx.mixin
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

        val chosenCellColor = "#085191".color.withOpacity(0.7)
        val missCellColor = "A4A5A6FF".color
        val hitCellColor = ORANGERED
        val titleCellColor = LIGHTSLATEGRAY
        val sunkCellColor = BLACK
        val readyColor: Color = MEDIUMSEAGREEN
        val wrongCellColor: Color = RED
        val buttonColor = "E8E3E4FF".color
        val defeatedCellColor = PINK
        val defeatedTitleCellColor = "A93638F4".color

        val currentPlayerListViewColors = PlayerListViewColors(DARKCYAN, DARKRED, DARKGREEN, GREY)
        val enemyListViewColors = PlayerListViewColors(chosenCellColor, RED, readyColor, BLACK)

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

        val fleetGrid by cssclass()

        val shipTypeLabel by cssclass()

        val emptyCell by cssclass()
        val fleetLabel by cssclass()

        val missCell by cssclass()
        val hitCell by cssclass()

        val shipBorderCell by cssclass()

        val titleCell by cssclass()

        val incorrectCell by cssclass()

        val chosenCell by cssclass()
        val sunkCell by cssclass()
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

        //---- components style ----------------------------------------------------------------------------------------
        label     + jetBrainFont
        textField + square
        button    + background(buttonColor) + jetBrainFont + fillParent + square
        odd       + background(WHITE)       + margin(1.px)
        even      + background(WHITE)       + margin(1.px)

        form {
            center()
            fontSize = 15.px
        }


        //---- labels style --------------------------------------------------------------------------------------------
        errorLabel + padding(20.px)
        fleetLabel + center + jetBrainFont + cellSize

        shipTypeLabel {
            backgroundRadius += box(25.px, 25.px, 5.px, 5.px)
        }

        //---- panes style ---------------------------------------------------------------------------------------------
        shipsTypesInfoPane + background(rgb(58, 132, 192, 0.35)) + margin(10.px)

        //---- buttons style -------------------------------------------------------------------------------------------
        readyButton + background(readyColor) + text(WHITE)

        //---- grids style ---------------------------------------------------------------------------------------------
        centerGrid          + center
        fleetGrid           + center                  + border
        gridMargin          + gridMargin(marginValue)
        animationGridMargin + gridMargin(10.px)

        //---- fleet  cells style --------------------------------------------------------------------------------------
        sunkCell          + background(sunkCellColor)
        defeatedTitleCell + background(defeatedTitleCellColor) + text(WHITE)
        chosenCell        + background(chosenCellColor)        + text(WHITE) + noBorder
        titleCell         + background(titleCellColor)         + text(WHITE) + border
        incorrectCell     + background(wrongCellColor)         + noBorder
        animationCell     + background(chosenCellColor)        + noBorder
        hitCell           + background(hitCellColor)           + border
        defeatedCell      + background(defeatedCellColor)      + border
        missCell          + radius(fleetCellSize / 4)          + border

        val shipBorderCellColor = RED
        (shipBorderCell + background(shipBorderCellColor.withOpacity(0.3))) {
            borderColor += box(shipBorderCellColor.withOpacity(0.15))
            borderWidth += box(fleetBorderWidth.px)
        }

        currentPlayerCell {
            border()
            focusTraversable = false
        }

        emptyCell {
            border()
            and(hover) {
                backgroundColor += chosenCellColor
            }
        }

        enemyCell {
            border()
            and(hover) {
                cursor = Cursor.CROSSHAIR
                backgroundColor += rgb(231, 64, 67, 0.5)
                backgroundImage += URI(targetIconPath)
            }
        }

        debugClass {
            gridMargin(10.px)
            borderColor += box(BLACK)
            borderWidth += box(fleetBorderWidth.px)
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




