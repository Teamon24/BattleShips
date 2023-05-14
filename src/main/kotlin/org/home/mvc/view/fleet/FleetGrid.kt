package org.home.mvc.view.fleet

import javafx.scene.layout.GridPane
import org.home.mvc.model.Coord
import org.home.mvc.model.Ships
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.components.GridPaneExtensions.getIndices
import org.home.mvc.view.fleet.FleetGridStyleComponent.addSelectionColor
import tornadofx.CssRule
import tornadofx.addClass

class FleetGrid : GridPane() {

    fun cell(cell: Coord): FleetCell {
        return getCell(cell) as FleetCell
    }

    fun addFleetCellClass(cssRule: CssRule) = onEachFleetCells { it.addClass(cssRule) }
    fun addTitleCellClass(cssRule: CssRule) = onEachTitleCells { it.addClass(cssRule) }

    fun addShips(ships: Ships): FleetGrid {
        ships.forEach { ship -> ship.forEach { cell(it).addSelectionColor() } }
        return this
    }

    inline fun onEachFleetCells(block: (FleetCell) -> Unit): FleetGrid {
        forEachFleetCells(block)
        return this
    }

    inline fun onEachTitleCells(block: (FleetCell) -> Unit): FleetGrid {
        forEachTitleCells(block)
        return this
    }

    inline fun forEachFleetCells(block: (FleetCell) -> Unit) {
        children
            .asSequence()
            .filterIsInstance<FleetCell>()
            .filter {
                val (row, col) = it.getIndices()
                row > 0 && col > 0
            }
            .forEach {
                block(it)
            }
    }

    inline fun forEachTitleCells(block: (FleetCell) -> Unit) {
        children
            .asSequence()
            .filterIsInstance<FleetCell>()
            .filter {
                val (row, col) = it.getIndices()
                row == 0 || col == 0
            }
            .forEach {
                block(it)
            }
    }


}