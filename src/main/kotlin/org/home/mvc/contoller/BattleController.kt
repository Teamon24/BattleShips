package org.home.mvc.contoller

import org.home.ApplicationProperties
import org.home.net.HitAction
import org.home.mvc.model.Coord
import tornadofx.Controller

abstract class BattleController: Controller() {
    protected val applicationProperties: ApplicationProperties by di()
    abstract fun onFleetCreationViewExit()
    abstract fun startBattle()

    protected abstract fun hitLogic(hitMessage: HitAction)

    fun hit(enemyToHit: String, hitCoord: Coord) {
        val hitMessage = HitAction(hitCoord, applicationProperties.currentPlayer, enemyToHit)
        hitLogic(hitMessage)
    }


}

