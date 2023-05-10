package org.home.mvc.contoller.server.action

import org.home.mvc.model.Coord

sealed class HasAShot(shooter: String, val shot: Coord): PlayerAction(shooter) {
    abstract val target: String
    val toStr get() = "$player' --$shot->> '$target'"
}

class ShotAction(shot: Coord, player: String, override val target: String): HasAShot(player, shot)

class HitAction(hit: Coord, player: String, override val target: String): HasAShot(player, hit) {
    constructor(shotAction: ShotAction): this(shotAction.shot, shotAction.player, shotAction.target)
}

class MissAction(miss: Coord, player: String, override val target: String): HasAShot(player, miss) {
    constructor(shotAction: ShotAction): this(shotAction.shot, shotAction.player, shotAction.target)
}