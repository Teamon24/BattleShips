package org.home.mvc.contoller

import javafx.scene.layout.GridPane
import org.home.mvc.view.component.GridPaneExtensions.getIndices
import org.home.mvc.view.component.GridPaneExtensions.setIndices
import org.home.mvc.view.component.GridPaneExtensions.transpose
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.style.AppStyles
import tornadofx.addClass

class ShipsTypesPane: GridPane() {
    private var transposed: Boolean = false

    init {
        addClass(AppStyles.gridMargin)
    }

    fun flip(): ShipsTypesPane {
        children.forEach {
            val (row, col) = it.getIndices()
            when(transposed) {
                true -> it.setIndices(row, col.opposite(0, 1))
                else -> it.setIndices(row.opposite(0, 1), col)
            }
        }

        return this
    }

    fun transposed() = transpose().apply { transposed = true }

    private fun Int.opposite(i: Int, i1: Int): Int {
        if (this == i)  return i1
        if (this == i1) return i
        throw RuntimeException("Number $this should be $i or $i1")
    }

    fun getTypeLabels() = children.filterIsInstance<ShipTypeLabel>()
}