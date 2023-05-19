package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUdate.CLASS
import org.home.style.AppStyles.Companion.currentPlayerLabel
import org.home.style.AppStyles.Companion.defeatedEmptyCell
import org.home.style.AppStyles.Companion.defeatedPlayerLabel
import org.home.style.AppStyles.Companion.defeatedShipNumberLabel
import org.home.style.AppStyles.Companion.defeatedShipTypeLabel
import org.home.style.AppStyles.Companion.defeatedTitleCell
import org.home.style.AppStyles.Companion.emptyCell
import org.home.style.AppStyles.Companion.fleetCell
import org.home.style.AppStyles.Companion.fullShipNumberLabel
import org.home.style.AppStyles.Companion.hitCell
import org.home.style.AppStyles.Companion.incorrectCell
import org.home.style.AppStyles.Companion.incorrectEmptyCell
import org.home.style.AppStyles.Companion.readyCell
import org.home.style.AppStyles.Companion.readyPlayerLabel
import org.home.style.AppStyles.Companion.readyShipNumberLabel
import org.home.style.AppStyles.Companion.readyShipTypeLabel
import org.home.style.AppStyles.Companion.readyTitleCell
import org.home.style.AppStyles.Companion.selectedCell
import org.home.style.AppStyles.Companion.shipBorderCell
import org.home.style.AppStyles.Companion.sunkCell
import org.home.style.AppStyles.Companion.titleCell
import org.home.style.StyleUtils.toggle
import tornadofx.addClass
import tornadofx.removeClass

object FleetGridStyleAddClass: FleetGridStyleComponent {
    override val type = CLASS


    override fun FleetCell.removeAnyColor(): FleetCell = this
        .removeClass(
            selectedCell,
            sunkCell,
            hitCell,
            readyCell,
            incorrectCell,
            shipBorderCell,
            titleCell
        )

    override fun FleetCellLabel.addSelectionColor() = addClass(selectedCell)
    override fun FleetCell.addIncorrectColor() = addClass(incorrectCell)
    override fun FleetCell.addBorderColor() = addClass(shipBorderCell)

    override fun FleetCell.removeSelectionColor() = removeClass(selectedCell)
    override fun FleetCell.removeIncorrectColor() = removeClass(incorrectCell)
    override fun FleetCell.removeBorderColor()    = removeClass(shipBorderCell)

    fun FleetCell.addIncorrectHover() = addClass(incorrectEmptyCell)
    fun FleetCell.removeIncorrectHover() = removeClass(incorrectEmptyCell)

    override fun FleetGrid.addSelectionColor(ship: Ship) {
        forEachCell(ship) {
            addSelectionColor()
            removeIncorrectColor()
        }
    }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) =
        toggleReady(player, fleetGrid, fleetReadiness, true)

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) =
        toggleReady(player, fleetGrid, fleetReadiness, false)

    override fun BattleView.defeated(
        defeated: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane
    ) {
        model {
            playerLabel(defeated).toggle(currentPlayerLabel, defeatedPlayerLabel)
        }

        fleetReadiness
            .forEachTypeLabel { it.toggle(selectedCell, defeatedShipTypeLabel) }
            .forEachNumberLabel { it.toggle(fullShipNumberLabel, defeatedShipNumberLabel) }

        fleetGrid
            .onEachTitleCells { it.toggle(titleCell, defeatedTitleCell) }
            .onEachFleetCells {
                it.coord.notIn(model.getShotsAt(defeated)).so {
                    it.toggle(emptyCell, defeatedEmptyCell)
                }
            }

    }

    private fun BattleView.toggleReady(
        player: String,
        fleetGrid: FleetGrid,
        fleetReadiness: ShipsTypesPane,
        isReady: Boolean
    ) {
        val playerUsualOrReady = isReady.getRule(currentPlayerLabel, readyPlayerLabel)
        val shipNumberUsualOrReady = isReady.getRule(fullShipNumberLabel, readyShipNumberLabel)
        val shipTypeUsualOrReady = isReady.getRule(selectedCell, readyShipTypeLabel)
        val usualOrReady = isReady.getRule(titleCell, readyTitleCell)
        val selectedOrReady = isReady.getRule(selectedCell, readyCell)
        val nonDeckClasses = isReady.getRule(emptyCell, fleetCell)

        model {
            playerLabel(player).toggle(playerUsualOrReady)
        }

        fleetReadiness
            .forEachTypeLabel { it.toggle(shipTypeUsualOrReady) }
            .forEachNumberLabel { it.toggle(shipNumberUsualOrReady) }

        fleetGrid
            .onEachTitleCells { it.toggle(usualOrReady) }
            .onEachFleetCells { cell ->
                model {
                    player
                        .hasDeck(cell.coord)
                        .then { selectedOrReady }
                        .or { nonDeckClasses }
                        .let { listOf(it) }
                        .forEach { cell.toggle(it) }
                }
            }
    }
}
