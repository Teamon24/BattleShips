package org.home.mvc.view.fleet

import javafx.scene.layout.GridPane
import org.home.mvc.model.Ships
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.components.GridPaneExtensions.getIndices
import org.home.mvc.view.fleet.FleetGridStyleComponent.addSelectionColor
import tornadofx.CssRule
import tornadofx.addClass

class FleetGrid : GridPane() {

    fun addShips(ships: Ships): FleetGrid {
        ships.forEach { ship -> ship.forEach { this.getCell(it).addSelectionColor() } }
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
        this.children
            .asSequence()
            .filterIsInstance<FleetCell>()
            .filter {
                val (row, col) = getIndices(it)

                row > 0 && col > 0
            }
            .forEach {
                block(it)
            }
    }

    inline fun forEachTitleCells(block: (FleetCell) -> Unit) {
        this.children
            .asSequence()
            .filterIsInstance<FleetCell>()
            .filter {
                val (row, col) = getIndices(it)
                row == 0 || col == 0
            }
            .forEach {
                block(it)
            }
    }

    fun addFleetCellClass(cssRule: CssRule): FleetGrid {
        return onEachFleetCells {
            it.addClass(cssRule)
        }
    }

    fun addTitleCellClass(cssRule: CssRule): FleetGrid {
        return onEachTitleCells {
            it.addClass(cssRule)
        }
    }
}