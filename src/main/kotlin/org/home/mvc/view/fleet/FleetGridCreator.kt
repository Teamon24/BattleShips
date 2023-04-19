package org.home.mvc.view.fleet

import javafx.scene.layout.GridPane
import org.home.mvc.view.components.cell
import org.home.style.AppStyles
import tornadofx.addChildIfPossible
import tornadofx.addClass

class FleetGridCreator(height: Int, width: Int) {

    val rowRange = 1..height
    val colRange = 1..width

    fun fleetGrid() = FleetGrid().also {
        it.addClass(AppStyles.fleetGrid)
        zeroTitleCell(it)
        digitTitle(it)
        letterTitle(it)
        fleetCells(it)
    }

    private fun zeroTitleCell(gridPane: GridPane) {
        cell(0, 0) { gridPane.titleCell(0, 0, "") }
    }

    private fun letterTitle(gridPane: GridPane) {
        for (i in 64 + rowRange) {
            val j = i - 64
            cell(0, j) {
                val text = "${Char(i)}"
                gridPane.titleCell(0, j, text)
            }
        }
    }

    private fun digitTitle(gridPane: GridPane) {
        for (i in rowRange) {
            cell(i, 0) {
                val text = "$i"
                gridPane.titleCell(i, 0, text)
            }
        }
    }

    private fun fleetCells(gridPane: GridPane) {
        println("creating fleet grid: (${rowRange.last}, ${colRange.last})")
        for (row in rowRange) {
            for (col in colRange) {
                cell(row, col) {
                    gridPane.fleetCell(row, col).also { gridPane.addChildIfPossible(it) }
                }
            }
        }
    }

    operator fun Int.plus(rowRange: IntRange): IntRange {
        return this + rowRange.first..this + rowRange.last
    }
}



