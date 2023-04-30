package org.home.mvc.contoller

import org.home.mvc.ApplicationProperties
import org.home.mvc.model.Coord
import org.home.net.action.Action
import org.home.net.action.NotReadyAction
import org.home.net.action.ShotAction
import tornadofx.Controller

abstract class BattleController: Controller() {
    protected val applicationProperties: ApplicationProperties by di()

    protected val currentPlayer = applicationProperties.currentPlayer

    fun onBattleViewExit() {
        send(NotReadyAction(currentPlayer))
    }

    abstract fun onWindowClose()
    abstract fun onFleetCreationViewExit()

    abstract fun startBattle()

    fun shot(enemy: String, shot: Coord) {
        val shotMessage = ShotAction(shot, currentPlayer, enemy)
        send(shotMessage)
    }

    abstract fun send(action: Action)
    abstract fun leaveBattle()
    abstract fun endGame(winner: String)
    abstract fun disconnect()
    abstract fun connectAndSend(ip: String, port: Int)
}

