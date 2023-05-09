package org.home.app

import javafx.geometry.Pos
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.paint.Color.WHITE
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import org.home.app.di.diDev
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.components.GridPaneExtensions.getIndices
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.animationGridMargin
import org.home.style.AppStyles.Companion.chosenCellColor
import org.home.style.ColorUtils.withOpacity
import org.koin.core.context.GlobalContext
import tornadofx.View
import tornadofx.addChildIfPossible
import tornadofx.addClass
import tornadofx.animateFill
import tornadofx.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sqrt


fun main() {
    GlobalContext.startKoin {
        modules(diDev("application", 0, 3))
    }
    launch<AnimationCheck>()
}

class AnimationCheck: AbstractApp<My2View>(My2View::class)

class My2View: View() {
    override val root = appViewAnimationGrid(50)

    companion object {

        private inline fun IntRange.doubleFor(body: (Int, Int) -> Unit) {
            for (i in this) {
                for (j in this) {
                    body(i, j)
                }
            }
        }
        private fun random() = Random().nextDouble(0.0, 1.0)
        private fun random(incl: Double, excl: Double) = Random().nextDouble(incl, excl)
        private const val twoPi = 2 * PI

        fun appViewAnimationGrid(rows: Int): GridPane {
            return object : GridPane() {
                init {
                    alignment = Pos.CENTER
                    addClass(animationGridMargin)
                    val cellSize = 40.0
                    (1..rows).doubleFor { row, col ->
                        cell(row, col) {
                            Rectangle(cellSize, cellSize)
                                .addClass(AppStyles.animationCell)
                                .addClass(AppStyles.fleetLabel)
                                .also { addChildIfPossible(it) }
                        }
                    }
                    waveCellFill(this, rows, rows, 30000.0)
                }
            }
        }

        fun randomCellFill(gridPane: GridPane, time: Double) {
            gridPane.children.forEach {
                val (i, j) = getIndices(it)
                val rectangle = gridPane.getCell(i, j) as Rectangle
                randomFillAnimation(rectangle, chosenCellColor.withOpacity(0.5), time)
            }
        }

        fun waveCellFill(gridPane: GridPane, rows: Int, cols: Int, time: Double) {
            gridPane.children.forEach {
                val (i, j) = getIndices(it)
                val rectangle = gridPane.getCell(i, j) as Rectangle
                waveAnimation(rectangle, i, j, rows, cols, rows / 2, chosenCellColor.withOpacity(0.5), time)
            }
        }

        private fun randomFillAnimation(label: Rectangle, color: Color, time: Double) {
            label.animateFill(
                time = Duration.millis(time),
                from = color,
                to = WHITE,
                play = true
            ) {
                cycleCount = 50
                playFrom(Duration.millis(time * random()))
            }

        }

        private fun waveAnimation(
            label: Rectangle,
            i: Int,
            j: Int,
            rows: Int,
            cols: Int,
            period: Int,
            color: Color,
            time: Double,
        ) {
            label.animateFill(
                time = Duration.millis(time),
                from = color,
                to = color.withOpacity(0.1),
                play = true
            ) {
                cycleCount = 50
                val d1 = time * twoPi / period
                val shiftI = i.toDouble() + rows/2
                val shiftJ = j.toDouble() + cols/2
                val millis = d1 * sqrt(shiftI * shiftI + shiftJ * shiftJ)
                playFrom(Duration.millis(millis + random(time/10, time/9)))
            }
        }
    }



}



