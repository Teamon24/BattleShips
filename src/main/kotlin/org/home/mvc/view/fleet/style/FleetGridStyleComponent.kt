package org.home.mvc.view.fleet.style

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.mvc.GameBean
import org.home.mvc.contoller.ShipsPane
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import tornadofx.CssRule

abstract class FleetGridStyleComponent: GameBean() {
    abstract val type: FleetGreedStyleUpdate

    enum class FleetGreedStyleUpdate {
        CLASS, TRANSITION, CSS, TIMELINE
    }

    abstract fun FleetCell.removeAnyColor(): FleetCell

    abstract fun FleetCell.addMiss(): FleetCell
    abstract fun FleetCell.addHit(): FleetCell
    abstract fun FleetCell.addSunk(): FleetCell

    abstract fun FleetCellLabel.addSelectionColor(): FleetCellLabel
    abstract fun FleetCell.addIncorrectColor(): FleetCell

    abstract fun FleetCell.addBorderColor(): FleetCell
    abstract fun FleetCell.removeSelectionColor(): FleetCell
    abstract fun FleetCell.removeIncorrectColor(): FleetCell

    abstract fun FleetCell.removeBorderColor(): FleetCell

    abstract fun FleetGrid.addSelectionColor(ship: Collection<Coord>)

    fun FleetGrid.removeIncorrectColor(constructed: Ship) { forEachCell(constructed) { removeIncorrectColor() } }
    fun FleetGrid.addIncorrectColor(ship: Collection<Coord>) = forEachCell(ship) { addIncorrectColor() }

    fun FleetGrid.removeAnyColor(collection: Collection<Coord>) = forEachCell(collection) { removeAnyColor() }

    fun FleetGrid.forEachCell(collection: Collection<Coord>, op: FleetCell.() -> FleetCell) =
        collection.forEach { cell(it).op() }

    abstract fun BattleView.ready(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane)
    abstract fun BattleView.notReady(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane)

    abstract fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, shipsPane: ShipsPane)

    fun Boolean.getRule(rule: CssRule, other: CssRule): Pair<CssRule, CssRule> {
        return Pair(
            choose(rule, other),
            choose(other, rule)
        )
    }
    private fun Boolean.choose(rule: CssRule, another: CssRule) = this.then(rule).or(another)

}