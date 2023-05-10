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
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Color.rgb
import javafx.scene.paint.Paint
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

@Suppress("MAGIC_NUMBER")
class AppStyles : Stylesheet() {

    class PlayerListViewColors(
        val turnColor: Color,
        val defeatedColor: Color,
        val readyColor: Color,
        val defaultColor: Color,
    )

    companion object {
        private val String.color get() = Paint.valueOf(this) as Color
        private const val targetIconPath = "/icons/target-3699.svg"

        val chosenCellColor = "#085191".color.withOpacity(0.7)
        val readyPlayerCellColor: Color = MEDIUMSEAGREEN
        val wrongCellColor: Color = RED
        val buttonColor = "EAE5E5FF".color

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

        const val fleetBorderWidth = 0.5

        val form by cssclass()
        val fleetGrid by cssclass()

        val shipTypeLabel by cssclass()

        val emptyCell by cssclass()
        val fleetLabel by cssclass()

        val missCell by cssclass()
        val hitCell by cssclass()

        val shipBorderCell by cssclass()

        val titleCell by cssclass()

        val incorrectCell by cssclass()
        val fieldSize by cssclass()

        val chosenCell by cssclass()
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
        fieldSize {
            maxWidth = 400.px
            maxHeight = 400.px
        }

        //---- components style ----------------------------------------------------------------------------------------
        label     + jetBrainFont
        textField + square
        button    + background(buttonColor) + jetBrainFont + fillParent + square
        odd       + background(WHITE)       + margin(1.px)
        even      + background(WHITE)       + margin(1.px)

        form {
            center()
            with(600.px) {
                prefHeight = this
                prefWidth = this
            }

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
        readyButton + background(readyPlayerCellColor) + text(WHITE)

        //---- grids style ---------------------------------------------------------------------------------------------
        centerGrid          + center
        fleetGrid           + center                  + border
        gridMargin          + gridMargin(marginValue)
        animationGridMargin + gridMargin(10.px)

        //---- fleet  cells style --------------------------------------------------------------------------------------
        chosenCell        + background(chosenCellColor)  + text(WHITE) + noBorder
        titleCell         + background(LIGHTSLATEGRAY)   + text(WHITE) + border
        defeatedTitleCell + background("A93638F4".color) + text(WHITE)
        incorrectCell     + background(wrongCellColor)   + noBorder
        animationCell     + background(chosenCellColor)  + noBorder
        missCell          + background("A4A5A6FF".color) + border
        hitCell           + background(ORANGERED)        + border
        defeatedCell      + background(PINK)             + border


        val shipBorderCellColor = RED
        (shipBorderCell + shipBorderCellColor.withOpacity(0.3).background) {
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




