package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.MissAction
import org.home.mvc.contoller.server.action.SinkingAction

sealed class ThereWasAShot(val hasAShot: HasAShot): HasAPlayer(hasAShot.player) {
    fun isMiss() = hasAShot is MissAction
}

class ShipWasHit(hitAction: HitAction): ThereWasAShot(hitAction)
class ShipWasSunk(sinkingAction: SinkingAction): ThereWasAShot(sinkingAction)
class ThereWasAMiss(missAction: MissAction): ThereWasAShot(missAction)