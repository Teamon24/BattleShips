package org.home.mvc.contoller


import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.model.addIfAbsent
import org.home.utils.extensions.ObservablePropertiesExtensions.copy
import org.home.utils.log

class ShipsTypesController: AbstractGameBean() {
    private val shipsTypes = model.shipsTypes.copy()

    fun validates(newShip: Collection<Coord>): Boolean {
        newShip.ifEmpty { return false }

        val newShipSize = newShip.size
        if (newShipSize > shipMaxLength()) return false

        val shipsNumber = shipsTypes[newShipSize]
        if (shipsNumber == 0) return false

        return true
    }

    private fun shipMaxLength() = shipsTypes.maxOf { it.key }

    fun add(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> shipsTypes[ship.size] = shipsTypes[ship.size]?.minus(1) }
            .forEach {
                model.shipsOf(currentPlayer).addIfAbsent(it.copy())
                log { "after addition ${ships.joinToString(",")}" }
                eventbus {
                    +ShipWasAdded(it.size, currentPlayer)
                }
            }

    }

    fun remove(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> shipsTypes[ship.size] = shipsTypes[ship.size]?.plus(1) }
            .forEach {
                eventbus {
                    +ShipWasDeleted(it.size, currentPlayer)
                }
                model.shipsOf(currentPlayer).remove(it)
                log { "after deletion $ships" }
            }
    }
}