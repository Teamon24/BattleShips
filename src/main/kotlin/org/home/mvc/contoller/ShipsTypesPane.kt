package org.home.mvc.contoller

import javafx.scene.layout.GridPane
import org.home.mvc.view.component.GridPaneExtensions.getIndices
import org.home.mvc.view.component.GridPaneExtensions.setIndices
import org.home.mvc.view.component.GridPaneExtensions.transpose
import org.home.mvc.view.fleet.ShipReadinessLabel
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.mvc.view.fleet.ShipsNumberLabel

abstract class ShipsPane: GridPane() {

    val labels = mutableMapOf<ShipTypeLabel, ShipsNumberLabel?>()

    enum class Type { WITH_SHIPS_NUMBERS, NO_SHIPS_NUMBERS }

    abstract fun forEachNumberLabel(block: (ShipTypeLabel, ShipsNumberLabel?) -> Unit)

    fun forEachNumberLabel(block: (ShipsNumberLabel) -> Unit) = apply { forEachLabel(block) }
    fun forEachTypeLabel(block: (ShipTypeLabel) -> Unit) = apply { forEachLabel(block) }

    private inline fun <reified T: ShipReadinessLabel> GridPane.getLabels() = children.filterIsInstance<T>()

    private inline fun <reified T: ShipReadinessLabel> GridPane.forEachLabel(block: (T) -> Unit) =
        apply { getLabels<T>().forEach(block) }
}

class FleetReadinessPane: ShipsPane() {
    override fun forEachNumberLabel(block: (ShipTypeLabel, ShipsNumberLabel?) -> Unit) {}
}

class ShipsTypesPane: ShipsPane() {
    private var transposed: Boolean = false
    private var flipped: Boolean = false

    fun flip(): ShipsTypesPane {
        flipped = true
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
        if (this == number) return other
        if (this == other)  return number
        throw RuntimeException("Number \"$this\" should be \"$number\" or \"$other\"")
    }

    override fun forEachNumberLabel(block: (ShipTypeLabel, ShipsNumberLabel?) -> Unit) {
        labels.forEach { (type, number) -> block(type, number) }
    }
}