package org.home.mvc.contoller

import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.Coord
import org.home.mvc.model.removeDestroyedDeck
import org.home.net.message.Action
import org.home.net.message.DefeatAction
import org.home.net.message.HitAction
import org.home.net.message.ShotAction
import org.home.utils.DSLContainer
import org.home.utils.extensions.BooleansExtensions.so

abstract class BattleController: AbstractGameController() {
    abstract fun send(action: Action)
    abstract fun send(actions: Collection<Action>)

    fun shot(enemy: String, shot: Coord) {
        val shotMessage = ShotAction(shot, currentPlayer, enemy)
        send(shotMessage)
    }

    fun send(addMessages: DSLContainer<Action>.() -> Unit) {
        val dslContainer = DSLContainer<Action>()
        dslContainer.addMessages()
        send(dslContainer.elements)
    }

    fun onHit(shotAction: ShotAction) {
        val ships = model.shipsOf(currentPlayer)
        ships.removeDestroyedDeck(shotAction.shot)
        val hitAction = HitAction(shotAction)

        val defeatAction = DefeatAction(shotAction.player, currentPlayer)

        send {
            + hitAction
            ships.isEmpty().so { + defeatAction }
        }

        eventbus {
            + ShipWasHit(hitAction)
            ships.isEmpty().so { + PlayerWasDefeated(defeatAction) }
        }
    }

    abstract fun startBattle()
    abstract fun leaveBattle()
    abstract fun endBattle()
    abstract fun disconnect()
    abstract fun connect(ip: String, port: Int)

    abstract fun onBattleViewExit()
    abstract fun onWindowClose()
}

