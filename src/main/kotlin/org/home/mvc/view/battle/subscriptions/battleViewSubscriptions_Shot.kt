package org.home.mvc.view.battle.subscriptions

import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.ThereWasAShot
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.openMessageWindow
import org.home.mvc.contoller.server.action.HasAShot
import org.home.style.AppStyles
import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then

import org.home.mvc.contoller.events.ShipWasSunk
import org.home.mvc.model.Coord
import org.home.mvc.model.isRightNextTo
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.addClass

internal fun BattleView.shipWasHit() {
    subscribe<ShipWasHit> { event ->
        logEvent(event, model)
        model.addShot(event.hasAShot)
        processShot(event) { shot.markHit(it) }
    }
}

internal fun BattleView.thereWasAMiss() {
    subscribe<ThereWasAMiss> { event ->
        logEvent(event, model)
        model.addShot(event.hasAShot)
        processShot(event) { shot.markMiss(it) }
    }
}

internal fun BattleView.shipWasSunk() {
    subscribe<ShipWasSunk> { event ->
        logEvent(event, model)
        event {
            model.addShot(hasAShot)
            processSunk(event)
        }
    }
}


fun Coord.markMiss(fleetGrid: FleetGrid) {
    fleetGrid.getCell(this).addClass(AppStyles.missCell)
}

fun Coord.markHit(fleetGrid: FleetGrid) {
    fleetGrid.getCell(this).removeAnyColor().addClass(AppStyles.hitCell)
}

fun Coord.markSunk(fleetGrid: FleetGrid) {
    log { "marking as sunk - $this" }
    fleetGrid.getCell(this).removeAnyColor().addClass(AppStyles.sunkCell)
}

fun Collection<Coord>.markSunk(fleetGrid: FleetGrid) {
    forEach { it.markSunk(fleetGrid) }
}

private inline fun BattleView.processShot(event: ThereWasAShot,
                                          crossinline markShot: HasAShot.(FleetGrid) -> Unit
) {
    event {
        hasAShot {
            val message = when (target == currentPlayer) {
                true -> "По вам ${isMiss() then "не " or ""}попал \"${player}\""
                else -> "\"${player}\" ${isMiss() then "промахнулся по" or "попал в"} \"${target}\""
            }

            openMessageWindow(message)

            val fleetGrid = when (target == currentPlayer) {
                true -> currentPlayerFleetGridPane.center as FleetGrid
                else -> enemiesFleetGridsPanes[target]!!
            }

            markShot(fleetGrid)
        }
    }
}

private fun BattleView.processSunk(event: ThereWasAShot) {
    event {
        hasAShot {
            val message = when (currentPlayer) {
                target -> "Ваш корабль потопил \"${player}\""
                player -> "Вы потопили корабль \"${target}\""
                else -> "\"${player}\" потопил корабль \"${target}\""
            }

            openMessageWindow(message)

            val fleetGrid = when (currentPlayer) {
                target -> currentPlayerFleetGridPane.center as FleetGrid
                else -> enemiesFleetGridsPanes[target]!!
            }

            shot.markSunk(fleetGrid)
            hashSetOf<Coord>().also {
                getRightNextTo(shot, it)
                it.markSunk(fleetGrid)
            }
        }
    }
}

private fun BattleView.getRightNextTo(
    coord: Coord,
    container: MutableSet<Coord>
) {
    var tempCont = getRightNextTo(coord)
    if (container.containsAll(tempCont)) return
    container.addAll(tempCont)

    tempCont.forEach {
        getRightNextTo(it, container)
    }
}

private fun BattleView.getRightNextTo(pair: Coord): MutableList<Coord> {
    return model.getHits().filter { it.isRightNextTo(pair) }.toMutableList()
}
