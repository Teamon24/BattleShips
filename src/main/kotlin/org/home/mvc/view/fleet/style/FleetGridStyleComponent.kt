package org.home.mvc.view.fleet.style

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import tornadofx.CssRule

interface FleetGridStyleComponent {
    val type: FleetGreedStyleUdate

    enum class FleetGreedStyleUdate {
        CLASS, TRANSITION, CSS
    }

    fun FleetCell.removeAnyColor(): FleetCell

    fun FleetCellLabel.addSelectionColor(): FleetCellLabel
    fun FleetCell.addIncorrectColor(): FleetCell
    fun FleetCell.addBorderColor(): FleetCell

    fun FleetCell.removeSelectionColor(): FleetCell
    fun FleetCell.removeIncorrectColor(): FleetCell
    fun FleetCell.removeBorderColor(): FleetCell

    fun FleetGrid.addSelectionColor(ship: Ship)

    fun FleetGrid.removeIncorrectColor(beingConstructedShip: Ship) {
        forEachCell(beingConstructedShip) { removeIncorrectColor() }
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
    fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane)

    fun Boolean.getRule(rule: CssRule, other: CssRule): Pair<CssRule, CssRule> {
        return Pair(
            choose(rule, other),
            choose(other, rule)
        )
    }

    private fun Boolean.choose(rule: CssRule, another: CssRule) = this.then(rule).or(another)
}