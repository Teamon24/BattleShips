package org.home.mvc.contoller.server

import home.extensions.AnysExtensions.invoke
import org.home.mvc.contoller.GameComponent
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.contoller.server.action.BattleEndAction

class BattleEndingComponent: GameComponent() {
    fun endBattle() {
        model {
            battleIsEnded = true
            eventbus {
                +BattleIsEnded(BattleEndAction(getWinner()))
            }
        }
    }
}