package org.home.mvc.contoller.server

import org.home.mvc.contoller.AbstractGameBean
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.contoller.server.action.BattleEndAction
import org.home.mvc.model.BattleModel.Companion.invoke

object BattleEventEmitter: AbstractGameBean() {
    fun endBattle() {
        model {
            battleIsEnded = true
            eventbus {
                +BattleIsEnded(BattleEndAction(getWinner()))
            }
        }
    }
}