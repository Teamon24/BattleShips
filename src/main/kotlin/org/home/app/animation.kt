package org.home.app

import javafx.animation.FillTransition
import javafx.animation.Transition
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
import org.home.style.ColorUtils.withOpacity
import org.koin.core.context.GlobalContext
import tornadofx.View
import tornadofx.addChildIfPossible
import tornadofx.addClass
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

        private val twoPi = 2 * PI

        fun appViewAnimationGrid(rows: Int): GridPane {
            return object : GridPane() {


                init {
                    alignment = Pos.CENTER
                    addClass(AppStyles.animationGridMargin)
                    val cellSize = 40.0
                    (1..rows).doubleFor { row, col ->
                        cell(row, col) {
                            Rectangle(cellSize, cellSize)
                                .addClass(AppStyles.animationCell)
                                .addClass(AppStyles.fleetLabel)
                                .also { addChildIfPossible(it) }
                        }
                    }

                    randomCellFill(this)

                }
            }
        }

        fun randomCellFill(gridPane: GridPane) {
            gridPane.children.forEach {
                val (i, j) = getIndices(it)
                val label = gridPane.getCell(i, j) as Rectangle
                randomAnimation(label, AppStyles.chosenCellColor.withOpacity(0.001))
            }
        }



        inline fun IntRange.doubleFor(body: (Int, Int) -> Unit) { for (i in this) { for (j in this) { body(i, j) } } }

        private fun randomAnimation(label: Rectangle, color: Color) {
            val (fill, d) = fillTransition(label, color)
            val duration = Duration.millis(d * random())
            fill.playFrom(duration)
        }

        private fun random() = Random().nextDouble(0.0, 1.0)

        private fun fillTransition(label: Rectangle, color: Color): Pair<Transition, Double> {
            val fill = FillTransition()
            fill.isAutoReverse = false
            fill.cycleCount = 50

            fill.shape = label
            fill.fromValue = color
            fill.toValue = WHITE
            val d = 10000.0
            fill.duration = Duration.millis(d)
            return Pair(fill, d)
        }


        private fun waveAnimation(gridPane: GridPane, size: Int, period: Int) {
            (1..size).doubleFor { i, j ->
                val label = gridPane.getCell(i, j) as Rectangle
                waveAnimation(label, i, j, size, size, period, AppStyles.chosenCellColor.withOpacity(0.01))
            }
        }

        private fun waveAnimation(label: Rectangle, i: Int, j: Int, rows: Int, cols: Int, period: Int, color: Color) {

            val (fill, d) = fillTransition(label, color)
            val d1 = d * twoPi / period
            val shiftI = i.toDouble() + rows/2
            val shiftJ = j.toDouble() + cols/2
            val millis = d1 * sqrt(shiftI * shiftI + shiftJ * shiftJ)
            val duration = Duration.millis(millis)
            fill.playFrom(duration)
        }
    }



}



