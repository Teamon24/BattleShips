package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.paint.Color
import org.home.app.ApplicationProperties.Companion.enemyFleetFillTransitionTime
import org.home.app.ApplicationProperties.Companion.fillingTransitionTime
import org.home.mvc.contoller.ShipsPane
import org.home.mvc.model.Coord
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TRANSITION
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.defeatedEmptyCellColor
import org.home.style.AppStyles.Companion.defeatedPlayerColor
import org.home.style.AppStyles.Companion.hitCellColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.AppStyles.Companion.missCellColor
import org.home.style.AppStyles.Companion.readyColor
import org.home.style.AppStyles.Companion.readyTitleColor
import org.home.style.AppStyles.Companion.selectedColor
import org.home.style.AppStyles.Companion.sunkCellColor
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import org.home.utils.ColorUtils.opacity
import org.home.utils.StyleUtils.backgroundColor
import org.home.utils.StyleUtils.fillBackground
import org.home.utils.StyleUtils.textColor
import tornadofx.style

object FleetGridStyleTransition: FleetGridStyleComponent() {
    override val type = TRANSITION

    override fun FleetCell.removeAnyColor(): FleetCell { TODO("Not yet implemented" ) }

    private val time = enemyFleetFillTransitionTime

    override fun FleetCell.addMiss() = apply { fillBackground(to = missCellColor, time = time) }
    override fun FleetCell.addHit()  = apply { fillBackground(to = hitCellColor, time = time) }
    override fun FleetCell.addSunk() = apply { fillBackground(to = sunkCellColor, time = time) }

    override fun FleetCellLabel.addSelectionColor(): FleetCellLabel { TODO("Not yet implemented" ) }

    override fun FleetCell.addIncorrectColor()    : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.addBorderColor()       : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.removeSelectionColor() : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.removeIncorrectColor() : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.removeBorderColor()    : FleetCell { TODO("Not yet implemented" ) }

    override fun FleetGrid.addSelectionColor(ship: Collection<Coord>) { TODO("Not yet implemented" ) }

    override fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {

        modelView.playerLabel(defeated)?.apply {
            fillBackground(backgroundColor, defeatedPlayerColor, time)
        }

        fleetGrid
             .onEachTitleCells { fleetCell ->
                fleetCell.style {
                    filling(fleetCell) {
                        millis = time
                        transition(fleetCell.backgroundColor, defeatedColor) { backgroundColor += it }
                        transition(fleetCell.textColor, sunkCellColor) { textFill = it }
                    }
                }
            }
            .onEachFleetCells {
                it.coord
                    .notIn(modelView.getShotsAt(defeated))
                    .so { it.fillBackground(to = defeatedEmptyCellColor, time = time) }
            }

        shipsPane
            .forEachTypeLabel { it.fillBackground(to = defeatedColor, time = time) }
            .forEachNumberLabel { label -> label.fillBackground(to = defeatedColor, time = time) }
    }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        val titleColor = readyTitleColor
        fleetGrid
            .onEachTitleCells { it.fillBackground(to = titleColor, time = fillingTransitionTime) }
            .onEachFleetCells {
                modelView {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.fillBackground(to = readyColor.opacity(0.9), time = fillingTransitionTime) }
                }
            }

        shipsPane.fill(titleColor)
    }

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        fleetGrid
            .onEachTitleCells { it.fillBackground(to = AppStyles.titleCellColor, time = fillingTransitionTime) }
            .onEachFleetCells {
                modelView {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.fillBackground(to = selectedColor, time = fillingTransitionTime) }
                        .otherwise { it.fillBackground(to = initialAppColor, time = fillingTransitionTime) }
                }
            }

        shipsPane.fill(selectedColor)
    }

    private fun ShipsPane.fill(color: Color) {
        forEachTypeLabel { it.fillBackground(it.backgroundColor, color, fillingTransitionTime) }
            .forEachNumberLabel { type, number ->
                number!! {
                    val constructed = text == "0"
                    constructed.thus {
                        fillBackground(backgroundColor, color, fillingTransitionTime)
                    }.otherwise {
                        fillBackground(backgroundColor, initialAppColor, fillingTransitionTime)
                    }
                }
            }
    }
}