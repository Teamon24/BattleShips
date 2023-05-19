package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.paint.Color
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.Ship
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUdate.CSS
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.selectedColor
import org.home.style.AppStyles.Companion.defeatedCellColor
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.incorrectCellColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.AppStyles.Companion.shipBorderCellColor
import org.home.style.AppStyles.Companion.titleCellColor
import org.home.style.ColorUtils.opacity
import tornadofx.style

object FleetGridStyleCssChange: FleetGridStyleComponent {
    override val type = CSS

    override fun FleetCell.removeAnyColor() = background(initialAppColor)
    override fun FleetCellLabel.addSelectionColor(): FleetCellLabel = background(selectedColor)
    override fun FleetCell.addIncorrectColor() = background(incorrectCellColor)
    override fun FleetCell.addBorderColor() = background(shipBorderCellColor)

    override fun FleetGrid.addSelectionColor(ship: Ship) {
        forEachCell(ship) { background(selectedColor) }
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
                        .thus { it.background(AppStyles.readyColor.opacity(0.9)) }
                        .otherwise { it.background(AppStyles.readyColor.opacity(0.1)) }
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
                        .thus { it.background(selectedColor) }
                        .otherwise { it.background(initialAppColor) }
                }
            }

        fleetReadiness
            .forEachTypeLabel { it.background(selectedColor) }
    }

    override fun BattleView.defeated(
        defeated: String,
        fleetGrid: FleetGrid,
        fleetReadiness: ShipsTypesPane
    ) {
        fleetGrid
            .onEachTitleCells {
                it.background(defeatedColor)
            }.onEachFleetCells {
                it.coord
                    .notIn(model.getShotsAt(defeated))
                    .so { it.background(defeatedCellColor) }
            }

        fleetReadiness
            .forEachTypeLabel {
                it.background(defeatedColor)
            }
    }
}