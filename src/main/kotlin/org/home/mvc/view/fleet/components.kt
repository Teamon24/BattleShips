package org.home.mvc.view.fleet

import javafx.event.EventTarget
import org.home.style.AppStyles
import tornadofx.CssRule
import tornadofx.add
import tornadofx.addClass


fun EventTarget.fleetCell(row: Int, col: Int, text: String = "") =
    cell(row, col, text)

fun EventTarget.titleCell(row: Int, col: Int, text: String) =
    cell(row, col, text, AppStyles.titleCell)

private fun EventTarget.cell(row: Int, col: Int, text: String, cssClass: CssRule) =
    FleetCell(row, col, text).also {
        it
            .addClass(AppStyles.fleetLabel)
            .addClass(cssClass)
        add(it)
    }

private fun EventTarget.cell(row: Int, col: Int, text: String) =
    FleetCell(row, col, text).also {
        it.addClass(AppStyles.fleetLabel)
        add(it)
    }
