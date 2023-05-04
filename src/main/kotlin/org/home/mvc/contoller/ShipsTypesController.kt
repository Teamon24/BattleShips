package org.home.mvc.contoller


import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.model.addIfAbsent
import org.home.utils.extensions.ObservablePropertiesExtensions.copy
import org.home.utils.log
import tornadofx.Controller

class ShipsTypesController: Controller() {

    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()

    private val battleShipsTypes = model.battleShipsTypes.copy()
    private val currentPlayer = applicationProperties.currentPlayer

    fun validates(newShip: Collection<Coord>): Boolean {
        newShip.ifEmpty { return false }

        val newShipSize = newShip.size
        if (newShipSize > shipMaxLength()) return false

        val shipsNumber = battleShipsTypes[newShipSize]
        if (shipsNumber == 0) return false

        return true
    }

    private fun shipMaxLength() = battleShipsTypes.maxOf { it.key }

    fun add(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> battleShipsTypes[ship.size] = battleShipsTypes[ship.size]?.minus(1) }
            .forEach {
                model.playersAndShips[currentPlayer]!!.addIfAbsent(it.copy())
                log { "after addition $ships" }
                eventbus {
                    +ShipWasAdded(it.size, currentPlayer)
                }
            }

    }

    fun remove(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> battleShipsTypes[ship.size] = battleShipsTypes[ship.size]?.plus(1) }
            .forEach {
                eventbus {
                    +ShipWasDeleted(it.size, currentPlayer)
                }
                model.playersAndShips[currentPlayer]!!.remove(it)
                log { "after deletion $ships" }
            }
    }
}