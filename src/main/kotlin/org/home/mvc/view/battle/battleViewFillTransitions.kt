package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import org.home.mvc.ApplicationProperties.Companion.defeatFillTransitionTime
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetGrid
import org.home.style.AppStyles.Companion.chosenCellColor
import org.home.style.AppStyles.Companion.defeatedCellColor
import org.home.style.AppStyles.Companion.defeatedTitleCellColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.AppStyles.Companion.readyColor
import org.home.style.AppStyles.Companion.sunkCellColor
import org.home.style.AppStyles.Companion.titleCellColor
import org.home.style.ColorUtils.withOpacity
import org.home.style.StyleUtils.backgroundColor
import org.home.style.StyleUtils.fillBackground
import org.home.style.StyleUtils.textColor
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import tornadofx.style

fun BattleView.readyFillTransition(
    player: String,
    fleetGrid: FleetGrid,
    fleetReadiness: ShipsTypesPane
) {
    val titleColor = readyColor.darker()
    fleetGrid
        .onEachTitleCells { fleetCell ->
            fleetCell.style {
                filling(fleetCell) {
                    millis = defeatFillTransitionTime
                    transition(fleetCell.backgroundColor, titleColor) { backgroundColor += it }
                }
            }
        }
        .onEachFleetCells {
            model {
                player.decks()
                    .contains(it.coord)
                    .thus { it.fillBackground(to = readyColor.withOpacity(0.9)) }
                    .otherwise { it.fillBackground(to = readyColor.withOpacity(0.1)) }
            }
        }

    fleetReadiness
        .getTypeLabels()
        .forEach {
            it.fillBackground(it.backgroundColor, titleColor)
        }
}

fun BattleView.notReadyFillTransition(
    player: String,
    fleetGrid: FleetGrid,
    fleetReadiness: ShipsTypesPane
) {
    fleetGrid
        .onEachTitleCells { fleetCell ->
            fleetCell.style {
                filling(fleetCell) {
                    millis = defeatFillTransitionTime
                    transition(fleetCell.backgroundColor, titleCellColor) { backgroundColor += it }
                }
            }
        }
        .onEachFleetCells {
            model {
                player.decks()
                    .contains(it.coord)
                    .thus { it.fillBackground(to = chosenCellColor) }
                    .otherwise { it.fillBackground(to = initialAppColor) }
            }
        }

    fleetReadiness
        .getTypeLabels()
        .forEach {
            it.fillBackground(it.backgroundColor, chosenCellColor)
        }
}

fun BattleModel.defeatedFillTransition(
    defeated: String,
    fleetGrid: FleetGrid,
    fleetReadiness: ShipsTypesPane
) {
    fleetGrid
        .onEachTitleCells { fleetCell ->
            fleetCell.style {
                filling(fleetCell) {
                    millis = defeatFillTransitionTime
                    transition(fleetCell.backgroundColor, defeatedTitleCellColor) { backgroundColor += it }
                    transition(fleetCell.textColor, sunkCellColor) { textFill = it }
                }
            }
        }
        .onEachFleetCells {
            it.coord
                .notIn(getShotsAt(defeated))
                .so {
                    it.fillBackground(to = defeatedCellColor)
                }
        }

    fleetReadiness
        .getTypeLabels()
        .forEach {
            it.fillBackground(it.backgroundColor, defeatedTitleCellColor)
        }
}
