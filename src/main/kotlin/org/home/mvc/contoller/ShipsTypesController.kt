package org.home.mvc.contoller


import home.extensions.AnysExtensions.name
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.model.addIfAbsent
import org.home.utils.log

class ShipsTypesController: AbstractGameBean() {
    private val shipsTypes = model.copyShipsTypes()

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
                log { "${this.name} ships addition ${ships.joinToString(",")}" }

                eventbus {
                    +ShipWasAdded(currentPlayer, it.size)
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
                log { "${this.name} ships deletion ${ships.joinToString(",")}" }
            }
    }
}