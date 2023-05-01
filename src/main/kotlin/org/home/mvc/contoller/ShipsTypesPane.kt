package org.home.mvc.contoller

import javafx.scene.layout.GridPane
import org.home.mvc.view.components.GridPaneExtensions
import org.home.mvc.view.components.GridPaneExtensions.getIndices
import org.home.mvc.view.components.GridPaneExtensions.setIndices
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
            val (row, col) = getIndices(it)
            if (transposed) {
                setIndices(it, row, col.opposite(0, 1))
            } else {
                setIndices(it, row.opposite(0, 1), col)
            }
        }

        return this
    }

    fun transpose(): ShipsTypesPane {
        transposed = true
        return GridPaneExtensions.transpose(this)
    }

    private fun Int.opposite(i: Int, i1: Int): Int {
        if (this == i) return i1
        if (this == i1) return i
        throw RuntimeException("Number $this should be $i or $i1")
    }

    fun getTypeLabels() = children.filterIsInstance<ShipTypeLabel>()

}