package org.home.mvc.model

import home.extensions.CollectionsExtensions.isEmpty

typealias Ship = MutableList<Coord>
typealias BeingConstructedShip = MutableList<Coord>

inline val Ship.isDestroyed get() = isEmpty

fun Ship.hasDecks(i: Int) = size == i
fun Collection<Coord>.toShip(): Ship = mutableListOf<Coord>().apply { addAll(this@toShip) }

fun Collection<Coord>.withinAnyBorder(ships: Ships) =
    ships.any { ship -> this@withinAnyBorder.any { it in ship.withBorder() } }

fun Ship.crosses(ships: Collection<Ship>) = ships.any { ship -> crosses(ship) }
private fun Ship.crosses(another: Ship) = any { deck -> deck in another }

fun Ship.withBorder() = flatMap { it.border() }.toHashSet()

fun Ship.border(rows: Int, cols: Int) =
    flatMap { it.border() }
        .filter { it.first <= rows && it.second <= cols }
        .filter { it.first >= 1 && it.second >= 1 }
        .toHashSet().also { it.removeAll(this) }

fun BeingConstructedShip.addIfAbsent(coord: Coord) {
    find { it == coord } ?: add(coord)
}

fun Ship.copy() = toMutableList()


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

inline fun <T> Iterable<T>.ship(transform: (T) -> Coord) = map { transform(it) }
