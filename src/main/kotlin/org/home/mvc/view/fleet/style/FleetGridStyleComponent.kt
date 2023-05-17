package org.home.mvc.view.fleet.style

import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetGrid
import org.home.style.AppStyles
import tornadofx.removeClass

interface FleetGridStyleComponent {

    fun FleetCell.removeAnyColor(): FleetCell

    fun FleetCell.addSelectionColor(): FleetCell
    fun FleetCell.addIncorrectColor(): FleetCell
    fun FleetCell.addBorderColor(): FleetCell

    fun FleetCell.removeSelectionColor(): FleetCell
    fun FleetCell.removeIncorrectColor(): FleetCell
    fun FleetCell.removeBorderColor(): FleetCell

    fun FleetGrid.addSelectionColor(ship: Ship)

    fun FleetGrid.removeIncorrectColor(beingConstructedShip: Ship) {
        forEachCell(beingConstructedShip) { removeClass(AppStyles.incorrectCell) }
    }

    fun FleetGrid.addIncorrectColor(ship: Ship) = forEachCell(ship) { addIncorrectColor() }

    fun FleetGrid.removeSelectionColor(collection: Collection<Coord>) =
        forEachCell(collection) { removeSelectionColor() }

    fun FleetGrid.addIncorrectColor(collection: Collection<Coord>) = forEachCell(collection) { addIncorrectColor() }
    fun FleetGrid.removeAnyColor(collection: Collection<Coord>) = forEachCell(collection) { removeAnyColor() }
    fun FleetGrid.removeBorderColor(collection: Collection<Coord>) = forEachCell(collection) { removeBorderColor() }
    fun FleetGrid.addBorderColor(collection: Collection<Coord>) = forEachCell(collection) { addBorderColor() }
    fun FleetGrid.forEachCell(collection: Collection<Coord>, op: FleetCell.() -> FleetCell) =
        collection.forEach { cell(it).op() }

    fun BattleView.ready(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane)
    fun BattleView.notReady(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane)
    fun BattleModel.defeatedFillTransition(defeated: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane)
}