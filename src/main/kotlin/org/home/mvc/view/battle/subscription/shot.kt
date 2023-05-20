package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ShipWasSunk
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.ThereWasAShot
import org.home.mvc.model.Coord
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.openMessageWindow
import org.home.utils.logEvent

internal fun BattleView.shipWasHit() = subscribe<ShipWasHit>(this::markHit)
internal fun BattleView.thereWasAMiss() = subscribe<ThereWasAMiss>(this::markMiss)

internal fun BattleView.shipWasSunk() {
    subscribe<ShipWasSunk> { event ->
        logEvent(event, modelView)
        event {
            modelView.addShot(hasAShot)
            processSunk(event)
        }
    }
}

fun BattleView.markMiss(fleetGrid: FleetGrid, shot: Coord) =
    fleetGrid.cell(shot).apply { shotStyleComponent { addMiss() } }

fun BattleView.markHit (fleetGrid: FleetGrid, shot: Coord) =
    fleetGrid.cell(shot).apply { shotStyleComponent { addHit() } }

fun BattleView.markSunk(fleetGrid: FleetGrid, shot: Coord) =
    fleetGrid.cell(shot).apply { shotStyleComponent { addSunk() } }

fun BattleView.markSunk(fleetGrid: FleetGrid, sunkShip: Collection<Coord>) =
    sunkShip.forEach { markSunk(fleetGrid, it) }

private inline fun <reified E: ThereWasAShot> BattleView.subscribe(crossinline markShot: (FleetGrid, Coord) -> Unit) {
    subscribe<E> { event ->
        logEvent(event, modelView)
        modelView.addShot(event.hasAShot)
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
            markShot(fleets(target), hasAShot.shot)
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

            val fleetGrid = fleets(target)

            markSunk(fleetGrid, shot)
            markSunk(fleetGrid, modelView.getShipBy(hasAShot))
        }
    }
}




