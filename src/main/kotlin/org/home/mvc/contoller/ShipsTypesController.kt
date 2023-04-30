package org.home.mvc.contoller


import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.events.DSLContainer.Companion.eventbus
import org.home.mvc.contoller.events.ShipWasConstructed
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.model.copy
import org.home.mvc.model.Coord
import tornadofx.Controller

class ShipsTypesController: Controller() {

    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()
    private val map = model.battleShipsTypes.copy()

    fun validates(newShip: Collection<Coord>): Boolean {
        newShip.ifEmpty { return false }

        val newShipSize = newShip.size
        if (newShipSize > shipMaxLength()) return false

        val shipsNumber = map[newShipSize]
        if (shipsNumber == 0) return false

        return true
    }

    private fun shipMaxLength() = map.maxOf { it.key }


    fun count(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> map[ship.size] = map[ship.size]?.minus(1) }
            .forEach {
                eventbus {
                    +ShipWasConstructed(it.size, applicationProperties.currentPlayer)
                }
            }

    }

    fun discount(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> map[ship.size] = map[ship.size]?.plus(1) }
            .forEach {
                eventbus {
                    +ShipWasDeleted(it.size, applicationProperties.currentPlayer)
                }
            }
    }
}