package org.home.mvc.model

import kotlin.math.abs

typealias Ships = MutableCollection<Ship>
typealias Coord = Pair<Int, Int>

fun xDistance(it: Coord, pair: Coord) = abs(it.first - pair.first)

fun yDistance(it: Coord, pair: Coord) = abs(it.second - pair.second)

fun Coord.isRightNextTo(coordinate: Coord): Boolean {
    val (xDistance, yDistance, rDist) = distance(coordinate)
    val isRightNext = rDist <= 1 && (xDistance == 1 || yDistance == 1)
    return isRightNext
}

fun Coord.isNextToSquare(coordinates: Ship): Boolean {
    return coordinates.any { distance(it).third == 2 }
}

fun Coord.isNextToSquare(coord: Coord): Boolean {
    return this.distance(coord).third == 2
}

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


private fun Coord.isRightNextTo(coordinates: Ship): Boolean {
    val isRightNext = coordinates.any {
        val (xDistance, yDistance, rDist) = distance(it)
        rDist <= 1 && (xDistance == 1 || yDistance == 1)
    }
    return isRightNext
}

private fun Coord.hasDistanceTo(coordinates: Ship): Boolean {
    val nearest = coordinates.nearestTo(this)
    return xDistance(nearest, this) > 1 && yDistance(nearest, this) > 1
}

private fun Ship.nearestTo(coord: Coord): Coord {
    return this.minByOrNull {
        val xDist = it.first - coord.first
        val yDist = it.second - coord.second
        xDist * xDist + yDist * yDist
    } ?: coord
}

