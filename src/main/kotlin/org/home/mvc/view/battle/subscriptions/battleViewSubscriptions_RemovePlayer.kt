package org.home.mvc.view.battle.subscriptions

import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.logEvent
import tornadofx.addClass


internal fun BattleView.playerWasDisconnected() {
    subscribe<PlayerWasDisconnected> { event ->
        logEvent(event)
        removePlayer(event.player)
        openMessageWindow { "${event.player} отключился" }
    }
}

internal fun BattleView.playerLeaved() {
    subscribe<PlayerLeaved> { event ->
        event {
            logEvent(this)
            removePlayer(player)
            openMessageWindow { "$player покинул ${model.battleIsEnded then "поле боя" or "бой"}" }
        }
    }
}

internal fun BattleView.playerWasDefeated() {
    subscribe<PlayerWasDefeated> { event ->
        logEvent(event)
        val defeated = event.player
        val shots = model.getShots(defeated)
        if (defeated != currentPlayer) {
            enemiesFleetsFleetGrids[defeated]!!.onEachFleetCells {
                if (it.coord !in shots) {
                    it.addClass(AppStyles.defeatedCell)
                }
            }
            enemiesFleetsFleetGrids[defeated]!!.disable()
        } else {
            currentPlayerFleetGrid.onEachFleetCells {
                if (it.coord !in model.getShots(currentPlayer)) {
                    it.addClass(AppStyles.defeatedCell)
                }
            }
        }

        model.defeatedPlayers.add(defeated)

        openMessageWindow {
            val whoDefeated = if (defeated == currentPlayer) "Вы" else defeated
            "$whoDefeated проиграл"
        }

        model {
            when {
                battleIsEnded -> battleController.endGame(getWinner())
                onePlayerLeft -> battleController.endGame(playersNames.first())
            }
        }
    }
}


private fun BattleView.removePlayer(player: String) {
    model.playersAndShips.remove(player)
    removeEnemy(player)
    if (model.playersNames.size == 1 && model.playersNames.first() == currentPlayer) {
        battleController.endGame(model.playersNames.first())
    }
}