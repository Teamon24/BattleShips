package org.home.mvc.contoller


import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.ifAbsent
import org.home.mvc.GameController
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.mvc.model.ShipsTypes
import org.home.mvc.model.crosses
import org.home.mvc.model.toShip
import org.home.mvc.model.withinAnyBorder
import org.home.utils.log

class ShipsTypesController : GameController() {
    private val shipsTypes = modelView.copyShipsTypes()

    val mapOp: (FleetEditEvent, ShipsTypes, Int) -> Unit = { event, map, key -> map[key] = event.op(map[key]!!) }

    fun validates(newShip: Collection<Coord>): Boolean {
        val ships = modelView.shipsOf(currentPlayer)
        ships.also {
            newShip.toShip().withinAnyBorder(it).so { return false }
            newShip.toShip().crosses(it).so { return false }
            log { "ships - $it" }
        }
        log { "fleetReadiness - $shipsTypes" }

        newShip.ifEmpty { return false }
        val newShipSize = newShip.size
        if (newShipSize > shipMaxLength()) return false

        val shipsNumber = shipsTypes[newShipSize]
        if (shipsNumber == 0) return false

        log { "is valid: $newShip" }
        return true
    }

    private fun shipMaxLength() = shipsTypes.maxOf { it.key }

    fun add(vararg newShips: Ship) {
        val ships = modelView.shipsOf(currentPlayer)
        newShips
            .filter { it.size != 0 }
            .forEach { newShip ->
                ships.ifAbsent(newShip) {
                    emitFleetEditEvent("addition", newShip, ::ShipWasAdded, MutableCollection<Ship>::add)
                }
            }
    }

    fun remove(vararg newShips: Ship) {
        val ships = modelView.shipsOf(currentPlayer)
        newShips
            .filter { it.size != 0 }
            .forEach { newShip ->
                ships.emitFleetEditEvent("deletion", newShip, ::ShipWasDeleted, MutableCollection<Ship>::remove)
            }
    }

    private fun MutableCollection<Ship>.emitFleetEditEvent(
        eventName: String,
        newShip: Ship,
        createEvent: (String, Int) -> FleetEditEvent,
        shipOperation: MutableCollection<Ship>.(Ship) -> Boolean
    ) {
        createEvent(currentPlayer, newShip.size).also {
            mapOp(it, shipsTypes, newShip.size)
            shipOperation(newShip)
            eventbus(it)
            log { "ship $eventName - $newShip" }
            log { "ships - ${modelView.shipsOf(currentPlayer)}" }
        }
    }
}