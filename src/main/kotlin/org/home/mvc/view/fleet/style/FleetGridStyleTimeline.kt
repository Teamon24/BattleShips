package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.so
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.model.Coord
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetCell
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TIMELINE
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.defeatedEmptyCellColor
import org.home.style.AppStyles.Companion.defeatedPlayerColor
import org.home.style.AppStyles.Companion.sunkCellColor
import org.home.style.TimelineDSL.keyValues

object FleetGridStyleTimeline : FleetGridStyleComponent() {
    override val type get() = TIMELINE

    override fun FleetCellLabel.addSelectionColor() : FleetCellLabel  { TODO("Not yet implemented") }
    override fun FleetCell.removeAnyColor()         : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addMiss()                : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addHit()                 : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addSunk()                : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addIncorrectColor()      : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addBorderColor()         : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.removeSelectionColor()   : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.removeIncorrectColor()   : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.removeBorderColor()      : FleetCell       { TODO("Not yet implemented") }
    override fun FleetGrid.addSelectionColor(ship: Collection<Coord>) { TODO("Not yet implemented") }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        TODO("Not yet implemented")
    }

    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        TODO("Not yet implemented")
    }

    override fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, fleetReadiness: ShipsTypesPane) {
        keyValues {
            modelView.playerLabel(defeated).apply {
                background(defeatedPlayerColor)
            }

            fleetGrid
                .onEachTitleCells {
                    it.background(defeatedColor)
                    it.text(sunkCellColor)
                }
                .onEachFleetCells {
                    it.coord.notIn(modelView.getShotsAt(defeated)).so {
                        it.background(defeatedEmptyCellColor)
                    }
                }

            fleetReadiness
                .forEachTypeLabel { it.background(defeatedColor) }
                .forEachNumberLabel { it.background(defeatedColor) }
        }
    }
}
