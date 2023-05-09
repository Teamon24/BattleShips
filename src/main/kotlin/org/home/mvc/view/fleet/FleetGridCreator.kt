package org.home.mvc.view.fleet

import javafx.scene.layout.GridPane
import org.home.mvc.contoller.AbstractGameBean
import org.home.mvc.view.components.GridPaneExtensions.cell
import tornadofx.addChildIfPossible

class FleetGridCreator: AbstractGameBean() {
    private val rowRange = 1.. model.height.value
    private val colRange = 1.. model.width.value

    fun titledFleetGrid() = FleetGrid().also {
        fleetCells(it, rowRange, colRange)
        zeroTitleCell(it)
        letterTitle(it, colRange)
        digitTitle(it, rowRange)
    }

    private fun zeroTitleCell(gridPane: GridPane) {
        cell(0, 0) { gridPane.titleCell(0, 0, "") }
    }

    private fun letterTitle(gridPane: GridPane, colRange: IntRange) {
        for (i in 64 + colRange) {
            val j = i - 64
            cell(0, j) {
                val text = "${Char(i)}"
                gridPane.titleCell(0, j, text)
            }
        }
    }

    private fun digitTitle(gridPane: GridPane, rowRange: IntRange) {
        for (i in rowRange) {
            cell(i, 0) {
                val text = "$i"
                gridPane.titleCell(i, 0, text)
            }
        }
    }

    private fun fleetCells(gridPane: GridPane, rowRange: IntRange, colRange: IntRange) {
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

    private fun IntRange.shift(shift: Int): IntRange {
        return IntRange(first + shift, last + shift)
    }
}



