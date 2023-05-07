package org.home.mvc.contoller

import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.Coord
import org.home.net.message.Action
import org.home.net.message.BattleEndAction
import org.home.net.message.ShotAction
import org.home.utils.DSLContainer
import org.home.utils.dslContainer

abstract class BattleController: AbstractGameController() {
    abstract fun send(action: Action)
    abstract fun send(actions: Collection<Action>)
    fun send(addMessages: DSLContainer<Action>.() -> Unit) = send(dslContainer(addMessages))

    fun shot(enemy: String, shot: Coord) = send(ShotAction(shot, currentPlayer, enemy))

    abstract fun connect(ip: String, port: Int)
    abstract fun disconnect()

    abstract fun startBattle()
    abstract fun leaveBattle()

    fun endBattle() {
        model {
            battleIsEnded = true
            eventbus {
                +BattleIsEnded(BattleEndAction(getWinner()))
            }
        }
    }

    abstract fun onBattleViewExit()
    abstract fun onWindowClose()
}

