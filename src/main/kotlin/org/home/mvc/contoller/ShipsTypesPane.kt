package org.home.mvc.contoller

import javafx.scene.layout.GridPane
import org.home.mvc.view.component.GridPaneExtensions.getIndices
import org.home.mvc.view.component.GridPaneExtensions.setIndices
import org.home.mvc.view.component.GridPaneExtensions.transpose
import org.home.mvc.view.fleet.ShipReadinessLabel
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.mvc.view.fleet.ShipsNumberLabel
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

    private fun Int.opposite(number: Int, other: Int): Int {
        if (this == number)  return other
        if (this == other) return number
        throw RuntimeException("Number \"$this\" should be \"$number\" or \"$other\"")
    }

    fun forEachTypeLabel(block: (ShipTypeLabel) -> Unit) = apply { forEachLabel(block) }
    fun forEachNumberLabel(block: (ShipsNumberLabel) -> Unit) = apply { forEachLabel(block) }

    private inline fun <reified T: ShipReadinessLabel> forEachLabel(block: (T) -> Unit) =
        apply { getLabels<T>().forEach(block) }

    private inline fun <reified T: ShipReadinessLabel> getLabels() = children.filterIsInstance<T>()

}