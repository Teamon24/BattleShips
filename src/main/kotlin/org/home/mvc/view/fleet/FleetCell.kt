package org.home.mvc.view.fleet

import javafx.event.EventTarget
import javafx.scene.control.Label
import org.home.mvc.model.Coord
import org.home.style.AppStyles
import org.home.utils.RomansDigits
import tornadofx.CssRule
import tornadofx.add
import tornadofx.addClass

sealed class FleetCellLabel(text: String = "") : Label(text) {
    init { addClass(AppStyles.fleetLabel) }
}

class FleetCell (row: Int, column: Int, text: String = ""): FleetCellLabel(text) {
    init { addClass(AppStyles.fleetCell) }
    val coord: Coord = row to column
}

sealed class ShipReadinessLabel(text: String): FleetCellLabel(text)

class ShipsNumberLabel(number: Int): ShipReadinessLabel(number.toString())
class ShipTypeLabel(type: Int): ShipReadinessLabel(RomansDigits.arabicToRoman(type))

fun EventTarget.fleetCell(row: Int, col: Int, text: String = "") =
    cell(row, col, text)

fun EventTarget.titleCell(row: Int, col: Int, text: String) =
    cell(row, col, text, AppStyles.titleCell)

private fun EventTarget.cell(row: Int, col: Int, text: String, cssClass: CssRule) =
    FleetCell(row, col, text).also { add(it.addClass(cssClass)) }

private fun EventTarget.cell(row: Int, col: Int, text: String) = FleetCell(row, col, text).also { add(it) }
