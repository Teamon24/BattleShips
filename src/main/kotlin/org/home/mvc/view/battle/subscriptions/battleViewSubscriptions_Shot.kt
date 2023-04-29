package org.home.mvc.view.battle.subscriptions

import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.ThereWasAShot
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.getCell
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.openMessageWindow
import org.home.net.action.HasAShot
import org.home.style.AppStyles
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.logEvent
import tornadofx.addClass

internal fun BattleView.shipWasHit() {
    subscribe<ShipWasHit> { event ->
        logEvent(event)
        model.addShot(event.hasAShot)
        processShot(event) { markHit(it) }
    }
}

internal fun BattleView.thereWasAMiss() {
    subscribe<ThereWasAMiss> { event ->
        logEvent(event)
        model.addShot(event.hasAShot)
        processShot(event) { markMiss(it) }
    }
}


fun HasAShot.markMiss(fleetGrid: FleetGrid) {
    fleetGrid.getCell(shot).addClass(AppStyles.missCell)
}

fun HasAShot.markHit(fleetGrid: FleetGrid) {
    fleetGrid.getCell(shot).removeAnyColor().addClass(AppStyles.hitCell)
}

private inline fun BattleView.processShot(event: ThereWasAShot,
                                          crossinline markShot: HasAShot.(FleetGrid) -> Unit
) {
    event {
        hasAShot {
            if (target == currentPlayer) {
                markShot(currentPlayerFleetGrid)
                openMessageWindow {
                    val part = isMiss() then "не " or ""
                    "По вам ${part}попал \"${shooter}\""
                }
            } else {
                markShot(enemiesFleetsFleetGrids[target]!!)
                openMessageWindow {
                    val actionString = isMiss() then "промахнулся по" or "попал в"
                    return@openMessageWindow "\"${player}\" $actionString \"${target}\""
                }
            }
        }
    }
}
