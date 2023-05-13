package org.home.mvc.model

import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.isEmpty
import org.home.utils.log

typealias Ships = MutableCollection<Ship>
inline fun Ships.areDestroyed(onTrue: () -> Unit) = isEmpty.so(onTrue)

class Ship
constructor(coordinates: Collection<Coord> = mutableListOf()) : ArrayList<Coord>(coordinates) {
    constructor(coordinate: Coord) : this(listOf(coordinate))
    constructor(vararg coordinates: Coord) : this(coordinates.toList())

    fun copy() = Ship(this.toMutableList())
    fun hasDecks(i: Int) = size == i
    inline val isDestroyed get() = isEmpty

    fun crosses(ships: Collection<Ship>) = ships.any { ship -> crosses(ship) }

    private fun crosses(another: Ship): Boolean {
        return this.any { deck -> deck in another }
    }

    fun withBorder(): HashSet<Coord> {
        return this
            .flatMap { it.border() }
            .toHashSet()
    }

    fun addIfAbsent(coord: Coord) {
        this.find { it == coord } ?: this.add(coord)
    }

    fun border(rows: Int, cols: Int): HashSet<Coord> {
        return this
            .flatMap { it.border() }
            .filter { it.first <= rows && it.second <= cols }
            .filter { it.first >= 1 && it.second >= 1 }
            .toHashSet().also { it.removeAll(this) }
    }

}


fun Ships.gotHitBy(shot: Coord) = any { shot in it }

fun Ships.removeAndGetBy(shot: Coord): Ship {
    val hitShip = first { it.contains(shot) }
    hitShip.remove(shot)
    if (hitShip.isEmpty()) {
        remove(hitShip)
    }

    return hitShip
}

fun Coord.withinAnyBorder(ships: Collection<Ship>) = ships.any { ship -> ship.withBorder().contains(this) }

fun Coord.withinAnyBorder(vararg ships: Ship) = ships.any { ship -> ship.withBorder().contains(this) }

fun Ships.addIfAbsent(ship: Ship) {
    ship.ifEmpty { return }
    find { it.size == ship.size && it.containsAll(ship) } ?: add(ship)
}

fun logShips(ships: Ships, action: String = "") {
    log { action }
    ships.forEach { log { it } }
}

inline fun <T> Iterable<T>.ship(transform: (T) -> Coord) = map { transform(it) }.toShip()

fun Collection<Coord>.toShip() = Ship(this)
fun Coord.toShip() = Ship(this)