package org.home.mvc.view.fleet.style

import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.so
import org.home.mvc.contoller.ShipsPane
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
import org.home.style.TimelineDSL.play

object FleetGridStyleTimeline : FleetGridStyleComponent() {
    override val type get() = TIMELINE

    override fun FleetCellLabel.addSelectionColor() : FleetCellLabel  { TODO("Not yet implemented") }
    override fun FleetCell.removeAnyColor()         : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addMiss()                : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addHit()                 : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addSunk()                : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.addIncorrectColor()      : FleetCell       { TODO("Not yet implemented") }
    override fun FleetCell.removeIncorrectColor()   : FleetCell       { TODO("Not yet implemented") }
    override fun FleetGrid.addSelectionColor(ship: Collection<Coord>) { TODO("Not yet implemented") }

    override fun BattleView.ready(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) { TODO("Not yet implemented") }
    override fun BattleView.notReady(player: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) { TODO("Not yet implemented") }

    override fun BattleView.defeated(defeated: String, fleetGrid: FleetGrid, shipsPane: ShipsPane) {
        play {
            modelView.playerLabel(defeated)?.apply { background(defeatedPlayerColor) }

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

            shipsPane
                .forEachTypeLabel { it.background(defeatedColor) }
                .forEachNumberLabel { label -> label.background(defeatedColor) }
        }
    }
}
