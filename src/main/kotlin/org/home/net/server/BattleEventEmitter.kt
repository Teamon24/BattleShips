package org.home.net.server

import org.home.mvc.contoller.AbstractGameController
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.net.message.BattleEndAction

object BattleEventEmitter: AbstractGameController() {
    fun endBattle() {
        model {
            battleIsEnded = true
            eventbus {
                +BattleIsEnded(BattleEndAction(getWinner()))
            }
        }
    }
}