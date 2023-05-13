package org.home.mvc.model

import kotlin.math.abs

typealias Coord = Pair<Int, Int>

fun Coord.aintHit(ships: Ships) = !this.hits(ships)
fun Coord.hits(ships: Ships) = ships.any { ship -> ship.contains(this) }

fun xDistance(it: Coord, pair: Coord) = abs(it.first - pair.first)

fun yDistance(it: Coord, pair: Coord) = abs(it.second - pair.second)

fun Coord.distance(it: Coord): Triple<Int, Int, Int> {
    val xDistance = xDistance(it, this)
    val yDistance = yDistance(it, this)
    val rDistance = xDistance * xDistance + yDistance * yDistance
    return Triple(xDistance, yDistance, rDistance)
}

fun Coord.border(): List<Coord> {
    val coords = arrayListOf<Coord>()
    return coords.also {
        it.add(first + 1 to second)
        it.add(first - 1 to second)
        it.add(first to second + 1)
        it.add(first to second - 1)
        it.add(first + 1 to second + 1)
        it.add(first - 1 to second + 1)
        it.add(first - 1 to second - 1)
        it.add(first + 1 to second - 1)
    }.filter { it.first > 0 || it.second > 0 }
}

fun Coord.isRightNextTo(coordinate: Coord): Boolean {
    val (xDistance, yDistance, rDist) = distance(coordinate)
    return rDist <= 1 && (xDistance == 1 || yDistance == 1)
}