package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import org.home.mvc.contoller.ShipsPane
import org.home.mvc.model.Coord
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CLASS
import org.home.style.AppStyles.Companion.currentPlayerLabel
import org.home.style.AppStyles.Companion.defeatedEmptyCell
import org.home.style.AppStyles.Companion.defeatedPlayerLabel
import org.home.style.AppStyles.Companion.defeatedShipNumberLabel
import org.home.style.AppStyles.Companion.defeatedShipTypeLabel
import org.home.style.AppStyles.Companion.defeatedTitleCell
import org.home.style.AppStyles.Companion.emptyCell
import org.home.style.AppStyles.Companion.enemyCell
import org.home.style.AppStyles.Companion.fleetCell
import org.home.style.AppStyles.Companion.fullShipNumberLabel
import org.home.style.AppStyles.Companion.hitCell
import org.home.style.AppStyles.Companion.incorrectCell
import org.home.style.AppStyles.Companion.incorrectEmptyCell
import org.home.style.AppStyles.Companion.missCell
import org.home.style.AppStyles.Companion.readyCell
import org.home.style.AppStyles.Companion.readyPlayerLabel
import org.home.style.AppStyles.Companion.readyShipNumberLabel
import org.home.style.AppStyles.Companion.readyShipTypeLabel
import org.home.style.AppStyles.Companion.readyTitleCell
import org.home.style.AppStyles.Companion.selectedCell
import org.home.style.AppStyles.Companion.sunkCell
import org.home.style.AppStyles.Companion.titleCell
import org.home.utils.StyleUtils.toggle
import tornadofx.addClass
import tornadofx.removeClass

object FleetGridStyleAddClass: FleetGridStyleComponent() {
    override val type = CLASS

    override fun FleetCell.removeAnyColor(): FleetCell = this
        .removeClass(
            selectedCell,
            sunkCell,
            hitCell,
            readyCell,
            incorrectCell,
            titleCell
        )

    override fun FleetCell.addMiss() = addClass(missCell)
    override fun FleetCell.addHit() = addClass(hitCell)
    override fun FleetCell.addSunk() = addClass(sunkCell)

    override fun FleetCellLabel.addSelectionColor() = addClass(selectedCell)
    override fun FleetCell.addIncorrectColor() = addClass(incorrectCell)

    override fun FleetCell.removeIncorrectColor() = removeClass(incorrectCell)

    fun FleetCell.addIncorrectHover() = addClass(incorrectEmptyCell)
    fun FleetCell.removeIncorrectHover() = removeClass(incorrectEmptyCell)

    override fun FleetGrid.addSelectionColor(ship: Collection<Coord>) {
        forEachCell(ship) {
            addSelectionColor()
            removeIncorrectColor()
        }
    }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) =
        toggleReady(player, fleetGrid, shipsPane, true)

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) =
        toggleReady(player, fleetGrid, shipsPane, false)

    override fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        modelView {
            playerLabel(defeated)?.toggle(currentPlayerLabel, defeatedPlayerLabel)
        }

        shipsPane
            .forEachTypeLabel { it.toggle(selectedCell, defeatedShipTypeLabel) }
            .forEachNumberLabel { label -> label.toggle(fullShipNumberLabel, defeatedShipNumberLabel) }

        fleetGrid
            .onEachTitleCells { it.toggle(titleCell, defeatedTitleCell) }
            .onEachFleetCells {
                modelView {
                    it.coord.notIn(getShotsAt(defeated)).so {
                        defeated.isCurrent
                            .then { it.addClass(defeatedEmptyCell) }
                            .or { it.toggle(enemyCell, defeatedEmptyCell) }
                    }
                }
            }
    }

    private fun BattleView.toggleReady(
        player: String,
        fleetGrid: FleetGrid,
        shipsPane: ShipsPane,
        isReady: Boolean
    ) {
        val shipNumberUsualOrReady = { constructed: Boolean ->
            isReady.getRule(
                constructed.then(fullShipNumberLabel).or(fleetCell),
                readyShipNumberLabel
            )
        }
        val playerUsualOrReady   = isReady.getRule(currentPlayerLabel, readyPlayerLabel)
        val shipTypeUsualOrReady = isReady.getRule(selectedCell, readyShipTypeLabel)
        val usualOrReady         = isReady.getRule(titleCell, readyTitleCell)
        val selectedOrReady      = isReady.getRule(selectedCell, readyCell)
        val nonDeckClasses       = isReady.getRule(emptyCell, fleetCell)


        modelView {
            playerLabel(player)?.toggle(playerUsualOrReady)
        }

        shipsPane
            .forEachTypeLabel { it.toggle(shipTypeUsualOrReady) }
            .forEachNumberLabel { type, number ->
                val constructed = number!!.text == "0"
                number.toggle(shipNumberUsualOrReady(constructed))
            }

        fleetGrid
            .onEachTitleCells { it.toggle(usualOrReady) }
            .onEachFleetCells { cell ->
                modelView {
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
