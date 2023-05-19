package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.paint.Color.BLACK
import org.home.mvc.ApplicationProperties.Companion.defeatFillTransitionTime
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.Coord
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TRANSITION
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.defeatedEmptyCellColor
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.defeatedPlayerColor
import org.home.style.AppStyles.Companion.defeatedShipNumberLabel
import org.home.style.AppStyles.Companion.hitCellColor
import org.home.style.AppStyles.Companion.missCellColor
import org.home.style.AppStyles.Companion.sunkCellColor
import org.home.style.ColorUtils.opacity
import org.home.style.CssUtils.noBorder
import org.home.style.StyleUtils.backgroundColor
import org.home.style.StyleUtils.fillBackground
import org.home.style.StyleUtils.textColor
import org.home.style.StyleUtils.toggle
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import tornadofx.style

object FleetGridStyleTransition: FleetGridStyleComponent {
    override val type = TRANSITION

    override fun FleetCell.removeAnyColor(): FleetCell { TODO("Not yet implemented" ) }

    override fun FleetCell.addMiss() = apply {
        style { noBorder() }
        fillBackground(to = missCellColor)
    }

    override fun FleetCell.addHit()  = apply {
        style { noBorder() }
        fillBackground(to = hitCellColor)
    }

    override fun FleetCell.addSunk() = apply {
        style { noBorder() }
        fillBackground(to = sunkCellColor)
    }

    override fun FleetCellLabel.addSelectionColor(): FleetCellLabel { TODO("Not yet implemented" ) }

    override fun FleetCell.addIncorrectColor()    : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.addBorderColor()       : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.removeSelectionColor() : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.removeIncorrectColor() : FleetCell { TODO("Not yet implemented" ) }
    override fun FleetCell.removeBorderColor()    : FleetCell { TODO("Not yet implemented" ) }

    override fun FleetGrid.addSelectionColor(ship: Collection<Coord>) { TODO("Not yet implemented" ) }

    override fun BattleView.defeated(
        defeated: String,
        fleetGrid: FleetGrid,
        fleetReadiness: ShipsTypesPane
    ) {

        model.playerLabel(defeated).apply {
            fillBackground(backgroundColor, defeatedPlayerColor)
        }

        fleetGrid
             .onEachTitleCells { fleetCell ->
                fleetCell.style {
                    filling(fleetCell) {
                        millis = defeatFillTransitionTime
                        transition(fleetCell.backgroundColor, defeatedColor) { backgroundColor += it }
                        transition(fleetCell.textColor, sunkCellColor) { textFill = it }
                    }
                }
            }
            .onEachFleetCells {
                it.coord
                    .notIn(model.getShotsAt(defeated))
                    .so {
                        it.fillBackground(to = defeatedEmptyCellColor)
                    }
            }

        fleetReadiness
            .forEachTypeLabel { it.fillBackground(it.backgroundColor, defeatedColor) }
            .forEachNumberLabel { it.fillBackground(it.backgroundColor, defeatedColor) }
    }

    override fun BattleView.ready(
        player: String,
        fleetGrid: FleetGrid,
        fleetReadiness: ShipsTypesPane
    ) {
        val titleColor = AppStyles.readyColor.darker()
        fleetGrid
            .onEachTitleCells { it.fillBackground(to = titleColor) }
            .onEachFleetCells {
                model {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.fillBackground(to = AppStyles.readyColor.opacity(0.9)) }
                }
            }

        fleetReadiness.forEachTypeLabel {
            it.fillBackground(it.backgroundColor, titleColor)
        }
    }

    override fun BattleView.notReady(
        player: String,
        fleetGrid: FleetGrid,
        fleetReadiness: ShipsTypesPane
    ) {
        fleetGrid
            .onEachTitleCells { it.fillBackground(to = AppStyles.titleCellColor) }
            .onEachFleetCells {
                model {
                    player.decks()
                        .contains(it.coord)
                        .thus { it.fillBackground(to = AppStyles.selectedColor) }
                        .otherwise { it.fillBackground(to = AppStyles.initialAppColor) }
                }
            }

        fleetReadiness.forEachTypeLabel {
            it.fillBackground(it.backgroundColor, AppStyles.selectedColor)
        }
    }
}