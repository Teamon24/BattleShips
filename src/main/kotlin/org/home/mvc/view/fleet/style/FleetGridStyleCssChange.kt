package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.paint.Color
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.chosenCellColor
import org.home.style.AppStyles.Companion.defeatedCellColor
import org.home.style.AppStyles.Companion.defeatedTitleCellColor
import org.home.style.AppStyles.Companion.incorrectCellColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.AppStyles.Companion.shipBorderCellColor
import org.home.style.AppStyles.Companion.titleCellColor
import org.home.style.ColorUtils.withOpacity
import tornadofx.style

object FleetGridStyleCssChange: FleetGridStyleComponent {

    override fun FleetCell.removeAnyColor() = background(initialAppColor)
    override fun FleetCell.addSelectionColor() = background(chosenCellColor)
    override fun FleetCell.addIncorrectColor() = background(incorrectCellColor)
    override fun FleetCell.addBorderColor() = background(shipBorderCellColor)

    override fun FleetGrid.addSelectionColor(ship: Ship) {
        forEachCell(ship) { background(chosenCellColor) }
    }

    override fun FleetCell.removeSelectionColor() = background(initialAppColor)
    override fun FleetCell.removeIncorrectColor() = background(incorrectCellColor)
    override fun FleetCell.removeBorderColor() = background(initialAppColor)

    private fun <T: FleetCellLabel> T.background(color: Color) = apply {
        style(append = true) { backgroundColor += color }
    }

    private fun FleetCell.text(color: Color) = style { textFill = color }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        val titleColor = AppStyles.readyColor.darker()
        fleetGrid
            .onEachTitleCells {
                it.background(titleColor)
            }
            .onEachFleetCells {
                model {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.background(AppStyles.readyColor.withOpacity(0.9)) }
                        .otherwise { it.background(AppStyles.readyColor.withOpacity(0.1)) }
                }
            }

        fleetReadiness
            .forEachTypeLabel { it.background }
    }

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        fleetGrid
            .onEachTitleCells {
                it.background(titleCellColor)
            }
            .onEachFleetCells {
                model {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.background(chosenCellColor) }
                        .otherwise { it.background(initialAppColor) }
                }
            }

        fleetReadiness
            .forEachTypeLabel { it.background(chosenCellColor) }
    }

    override fun BattleModel.defeatedFillTransition(
        defeated: String,
        fleetGrid: FleetGrid,
        fleetReadiness: ShipsTypesPane
    ) {
        fleetGrid
            .onEachTitleCells {
                it.background(defeatedTitleCellColor)
            }.onEachFleetCells {
                it.coord
                    .notIn(getShotsAt(defeated))
                    .so { it.background(defeatedCellColor) }
            }

        fleetReadiness
            .forEachTypeLabel {
                it.background(defeatedTitleCellColor)
            }
    }
}