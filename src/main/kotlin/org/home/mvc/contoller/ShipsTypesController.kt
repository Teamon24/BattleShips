package org.home.mvc.contoller

import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import org.home.utils.aliases.Coord
import org.home.mvc.model.Ship
import tornadofx.Controller

class ShipsTypesController(private val map: ObservableMap<Int, Int> = FXCollections.observableHashMap()): Controller() {

    fun validates(newShip: Collection<Coord>): Boolean {
        newShip.ifEmpty { return false }

        val newShipSize = newShip.size
        if (newShipSize > shipMaxLength()) return false

        val shipsNumber = this[newShipSize]
        if (shipsNumber == 0) return false

        return true
    }

    private fun shipMaxLength() = map.maxOf { it.key }

    fun count(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship ->
                map[ship.size] = map[ship.size]?.minus(1)
            }.forEach {
                fire(ShipCountEvent(it.size))
            }

    }

    fun discount(vararg ships: Ship) {
        ships
            .filter { it.size != 0 }
            .onEach { ship -> map[ship.size] = map[ship.size]?.plus(1) }
            .forEach {
                fire(ShipDiscountEvent(it.size))
            }

    }

    fun discount(ships: List<Ship>) {
        ships.forEach { ship ->
            map[ship.size] = map[ship.size]?.plus(1)
        }
    }

    operator fun get(index: Int) = map[index]
}