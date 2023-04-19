package org.home.mvc.view.fleet

import javafx.scene.control.Label
import org.home.utils.aliases.Coord
import org.home.style.AppStyles
import tornadofx.addClass

class FleetCell
constructor(row: Int, column: Int, text: String = "") : FleetCellLabel(text) {
    val coord: Coord = row to column
}

open class FleetCellLabel(text: String = "") : Label(text) {
    init { addClass(AppStyles.fleetLabel) }
}