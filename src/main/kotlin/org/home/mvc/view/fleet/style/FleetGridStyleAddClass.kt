package org.home.mvc.view.fleet.style

import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetGrid
import org.home.style.AppStyles.Companion.chosenCell
import org.home.style.AppStyles.Companion.hitCell
import org.home.style.AppStyles.Companion.incorrectCell
import org.home.style.AppStyles.Companion.shipBorderCell
import org.home.style.AppStyles.Companion.sunkCell
import org.home.style.AppStyles.Companion.titleCell
import tornadofx.addClass
import tornadofx.removeClass

object FleetGridStyleAddClass: FleetGridStyleComponent {

    override fun FleetCell.removeAnyColor(): FleetCell = this
        .removeClass(
            chosenCell,
            sunkCell,
            hitCell,
            incorrectCell,
            shipBorderCell,
            titleCell
        )

    override fun FleetCell.addSelectionColor() = addClass(chosenCell)
    override fun FleetCell.addIncorrectColor() = addClass(incorrectCell)
    override fun FleetCell.addBorderColor()    = addClass(shipBorderCell)

    override fun FleetCell.removeSelectionColor() = removeClass(chosenCell)
    override fun FleetCell.removeIncorrectColor() = removeClass(incorrectCell)
    override fun FleetCell.removeBorderColor()    = removeClass(shipBorderCell)

    override fun FleetGrid.addSelectionColor(ship: Ship) {
        forEachCell(ship) { removeIncorrectColor().addSelectionColor() }
    }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        TODO("Not yet implemented")
    }

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        TODO("Not yet implemented")
    }

    override fun BattleModel.defeatedFillTransition(defeated: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        TODO("Not yet implemented")
    }
}
