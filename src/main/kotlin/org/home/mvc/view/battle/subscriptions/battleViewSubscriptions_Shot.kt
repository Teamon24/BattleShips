package org.home.mvc.view.battle.subscriptions

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ShipWasSunk
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.ThereWasAShot
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Coord
import org.home.mvc.model.getRightNextTo
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles.Companion.hitCellColor
import org.home.style.AppStyles.Companion.missCell
import org.home.style.AppStyles.Companion.missCellColor
import org.home.style.AppStyles.Companion.sunkCellColor
import org.home.style.StyleUtils.fillBackground
import org.home.utils.logEvent
import tornadofx.addClass

internal fun BattleView.shipWasHit() = subscribe<ShipWasHit>(::markHit)
internal fun BattleView.thereWasAMiss() = subscribe<ThereWasAMiss>(::markMiss)

internal fun BattleView.shipWasSunk() {
    subscribe<ShipWasSunk> { event ->
        logEvent(event, model)
        event {
            model.addShot(hasAShot)
            processSunk(event)
        }
    }
}

fun markMiss(fleetGrid: FleetGrid, shot: Coord) =
    fleetGrid
        .cell(shot)
        .apply { addClass(missCell) }
        .fillBackground(to = missCellColor)

fun markHit (fleetGrid: FleetGrid, shot: Coord) = fleetGrid.cell(shot).fillBackground(to = hitCellColor)

fun FleetGrid.markSunk(shot: Coord) = cell(shot).fillBackground(to = sunkCellColor)
fun FleetGrid.markSunk(sunkShip: Collection<Coord>) = sunkShip.forEach { markSunk(it) }

private inline fun <reified E: ThereWasAShot> BattleView.subscribe(crossinline markShot: (FleetGrid, Coord) -> Unit) {
    subscribe<E> { event ->
        logEvent(event, model)
        model.addShot(event.hasAShot)
        processShot(event, markShot)
    }
}

private inline fun BattleView.processShot(event: ThereWasAShot,
                                          markShot: (FleetGrid, Coord) -> Unit) {
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

            markShot(fleetGrid, hasAShot.shot)
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

            fleetGrid{
                markSunk(shot)
                hashSetOf<Coord>().also {
                    model.getRightNextTo(shot, it)
                    markSunk(it)
                }
            }
        }
    }
}

private fun BattleModel.getRightNextTo(
    coord: Coord,
    container: MutableSet<Coord>
) {
    var tempCont = getHits().getRightNextTo(coord)
    if (container.containsAll(tempCont)) return
    container.addAll(tempCont)

    tempCont.forEach {
        getRightNextTo(it, container)
    }
}


