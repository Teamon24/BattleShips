package org.home.mvc.contoller

import org.home.ApplicationProperties
import org.home.net.HitMessage
import org.home.utils.aliases.Coord
import tornadofx.Controller

abstract class BattleController: Controller() {
    protected val applicationProperties: ApplicationProperties by di()
    abstract fun onFleetCreationViewExit()
    abstract fun startBattle()

    protected abstract fun hitLogic(hitMessage: HitMessage)

    fun hit(enemyToHit: String, hitCoord: Coord) {
        val hitMessage = HitMessage(hitCoord, applicationProperties.currentPlayer, enemyToHit)
        hitLogic(hitMessage)
    }


}

