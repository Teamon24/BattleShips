package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.simpleName
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.paint.Color
import org.home.mvc.contoller.ShipsPane
import org.home.mvc.model.Coord
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CSS
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.defeatedEmptyCellColor
import org.home.style.AppStyles.Companion.incorrectCellColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.AppStyles.Companion.readyColor
import org.home.style.AppStyles.Companion.readyTitleColor
import org.home.style.AppStyles.Companion.selectedColor
import org.home.style.AppStyles.Companion.titleCellColor
import org.home.utils.ColorUtils.opacity
import tornadofx.style

object FleetGridStyleCssChange: FleetGridStyleComponent() {
    override val type = CSS

    override fun FleetCell.removeAnyColor() = background(initialAppColor)

    override fun FleetCell.addMiss(): FleetCell { TODO("Not yet implemented") }
    override fun FleetCell.addHit(): FleetCell { TODO("Not yet implemented") }
    override fun FleetCell.addSunk(): FleetCell { TODO("Not yet implemented") }

    override fun FleetCellLabel.addSelectionColor(): FleetCellLabel = background(selectedColor)
    override fun FleetCell.addIncorrectColor() = background(incorrectCellColor)

    override fun FleetGrid.addSelectionColor(ship: Collection<Coord>) {
        forEachCell(ship) { background(selectedColor) }
    }

    override fun FleetCell.removeIncorrectColor() = background(incorrectCellColor)

    fun <T: FleetCellLabel> T.background(color: Color) =
        apply { style(append = true) { backgroundColor += color } }

    private fun FleetCell.text(color: Color) = style { textFill = color }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        fleetGrid
            .onEachTitleCells { it.background(readyTitleColor) }
            .onEachFleetCells {
                modelView {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.background(readyColor.opacity(0.9)) }
                        .otherwise { it.background(readyColor.opacity(0.1)) }
                }
            }

        shipsPane
            .forEachTypeLabel { it.background(readyTitleColor) }
            .forEachNumberLabel { _ -> TODO("${this.simpleName}#forEachNumberLabel") }
    }

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        fleetGrid
            .onEachTitleCells {
                it.background(titleCellColor)
            }
            .onEachFleetCells {
                modelView {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.background(selectedColor) }
                        .otherwise { it.background(initialAppColor) }
                }
            }

        shipsPane
            .forEachTypeLabel { it.background(selectedColor) }
            .forEachNumberLabel { _ -> TODO("${this.simpleName}#forEachNumberLabel") }
    }

    override fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        fleetGrid
            .onEachTitleCells {
                it.background(defeatedColor)
            }.onEachFleetCells {
                it.coord
                    .notIn(modelView.getShotsAt(defeated))
                    .so { it.background(defeatedEmptyCellColor) }
            }

        shipsPane
            .forEachTypeLabel { it.background(defeatedColor) }
            .forEachNumberLabel { _ -> TODO("${this.simpleName}#forEachNumberLabel") }
    }
}