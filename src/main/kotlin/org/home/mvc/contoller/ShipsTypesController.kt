package org.home.mvc.contoller


import home.extensions.CollectionsExtensions.ifAbsent
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.utils.log

class ShipsTypesController : GameController() {
    private val shipsTypes = model.copyShipsTypes()

    fun validates(newShip: Collection<Coord>): Boolean {
        model.log { "ships: ${shipsOf(currentPlayer)}" }
        log { "$shipsTypes" }
        newShip.ifEmpty { return false }

        val newShipSize = newShip.size
        if (newShipSize > shipMaxLength()) return false

        val shipsNumber = shipsTypes[newShipSize]
        if (shipsNumber == 0) return false

        log { "new ship is valid: $newShip" }
        return true
    }

    private fun shipMaxLength() = shipsTypes.maxOf { it.key }

    fun add(vararg newShips: Ship) {
        newShips
            .filter { it.size != 0 }
            .forEach { newShip ->
                val ships = model.shipsOf(currentPlayer)
                ships.ifAbsent(newShip) {
                    ShipWasAdded(currentPlayer, newShip.size).also {
                        it.mapOp(shipsTypes, newShip.size)
                        add(newShip)
                        eventbus(it)
                        log { "ship addition - $newShip" }
                    }
                }
            }
    }

    fun remove(vararg newShips: Ship) {
        newShips
            .filter { it.size != 0 }
            .forEach { newShip ->
                val ships = model.shipsOf(currentPlayer)
                ShipWasDeleted(currentPlayer, newShip.size).also {
                    it.mapOp(shipsTypes, newShip.size)
                    ships.remove(newShip)
                    eventbus(it)
                    log { "ship deletion - $newShip" }
                }
            }
    }
}