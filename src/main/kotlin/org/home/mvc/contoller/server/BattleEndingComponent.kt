package org.home.mvc.contoller.server

import home.extensions.AnysExtensions.invoke
import org.home.mvc.contoller.AbstractGameBean
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.contoller.server.action.BattleEndAction

class BattleEndingComponent: AbstractGameBean() {
    fun endBattle() {
        model {
            battleIsEnded = true
            eventbus {
                +BattleIsEnded(BattleEndAction(getWinner()))
            }
        }
    }
}