package org.home.mvc.view.fleet

import javafx.scene.control.Label
import org.home.mvc.model.Coord
import org.home.style.AppStyles
import tornadofx.addClass

open class FleetCellLabel(text: String = "") : Label(text) {
    init {
        addClass(AppStyles.fleetLabel)
    }
}

class FleetCell (row: Int, column: Int, text: String = "") : FleetCellLabel(text) {
    val coord: Coord = row to column
}
