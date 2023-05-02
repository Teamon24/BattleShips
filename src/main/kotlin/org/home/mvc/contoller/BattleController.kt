package org.home.mvc.contoller

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Coord
import org.home.mvc.model.removeDestroyedDeck
import org.home.net.action.Action
import org.home.net.action.DefeatAction
import org.home.net.action.HitAction
import org.home.net.action.ShotAction
import org.home.utils.DSLContainer
import org.home.utils.extensions.BooleansExtensions.so
import tornadofx.Controller

abstract class BattleController(applicationProperties: ApplicationProperties): Controller() {
    protected val currentPlayer = applicationProperties.currentPlayer
    protected val model: BattleModel by di()

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
        val ships = model.playersAndShips[currentPlayer]!!
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
    abstract fun connectAndSend(ip: String, port: Int)

    abstract fun onBattleViewExit()
    abstract fun onWindowClose()
    abstract fun onFleetCreationViewExit()
}

