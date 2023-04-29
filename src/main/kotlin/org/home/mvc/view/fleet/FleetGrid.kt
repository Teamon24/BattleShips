package org.home.mvc.view.fleet

import javafx.scene.layout.GridPane
import org.home.mvc.model.Ships
import org.home.mvc.view.components.getCell
import org.home.mvc.view.components.getIndices
import org.home.mvc.view.fleet.FleetGridStyleComponent.addSelectionColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.net.action.HasAShot
import org.home.style.AppStyles
import tornadofx.CssRule
import tornadofx.addClass

class FleetGrid: GridPane() {

    fun addShips(ships: Ships): FleetGrid {
        ships.forEach { ship -> ship.forEach { this.getCell(it).addSelectionColor() } }
        return this
    }

    inline fun onEachFleetCells(block: (FleetCell) -> Unit): FleetGrid {
        forEachFleetCells(block)
        return this
    }

    inline fun forEachFleetCells(block: (FleetCell) -> Unit) {
        this.children.forEach {
            val (row, col) = getIndices(it)
            if (row > 0 && col > 0) {
                it as FleetCell
                block(it)
            }
        }
    }

    fun addFleetCellClass(cssRule: CssRule): FleetGrid {
        return onEachFleetCells {
            it.addClass(cssRule)
        }
    }
}