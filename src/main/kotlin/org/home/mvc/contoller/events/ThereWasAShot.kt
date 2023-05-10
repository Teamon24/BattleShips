package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.MissAction

sealed class ThereWasAShot(val hasAShot: HasAShot): HasAPlayer(hasAShot.player) {
    fun isMiss() = hasAShot is MissAction
}

class ShipWasHit(hitAction: HitAction): ThereWasAShot(hitAction)
class ThereWasAMiss(missAction: MissAction): ThereWasAShot(missAction)