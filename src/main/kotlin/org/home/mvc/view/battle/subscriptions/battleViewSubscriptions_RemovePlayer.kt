package org.home.mvc.view.battle.subscriptions

import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.battleIsEnded
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.addClass

internal fun BattleView.playerWasDisconnected() {
    subscribe<PlayerWasDisconnected> { event ->
        logEvent(event, model)
        removePlayer(event.player)
        openMessageWindow { "${event.player} отключился" }
    }
}

internal fun BattleView.playerLeaved() {
    subscribe<PlayerLeaved> {
        logEvent(it, model)
        removePlayer(it.player)
        openMessageWindow { "${it.player} покинул ${model.battleIsEnded then "поле боя" or "бой"}" }
    }
}

internal fun BattleView.playerWasDefeated() {
    subscribe<PlayerWasDefeated> { event ->
        logEvent(event, model)
        val defeated = event.player
        model.defeatedPlayers.add(defeated)
        val shots = model.getShots(defeated)

        val fleetGrid = when(defeated != currentPlayer) {
            true -> enemiesFleetsFleetGrids[defeated]!!.disable()
            else -> currentPlayerFleetGrid
        }

        fleetGrid
            .addTitleCellClass(AppStyles.defeatedTitleCell)
            .onEachFleetCells {
                if (it.coord !in shots) { it.addClass(AppStyles.defeatedCell) }
            }

        val fleetReadiness = when(defeated != currentPlayer) {
            true -> enemiesFleetsReadinessPanes[defeated]!!
            else -> currentPlayerFleetReadiness
        }

        fleetReadiness.getTypeLabels().forEach { it.addClass(AppStyles.defeatedTitleCell) }

        openMessageWindow {
            val args = when (defeated) {
                currentPlayer -> listOf("Вы", "и").asIterator()
                else -> listOf(defeated, "").asIterator()
            }
            "${args.next()} проиграл${args.next()}"
        }

        if (defeated == currentPlayer)
            battleViewExitButton.text = "Покинуть поле боя"

        model.battleIsEnded.so { battleController.endBattle() }
        if (model.playersNames.size == 1) {
            battleController.disconnect()
        }
    }
}

private fun BattleView.removePlayer(player: String) {
    model {
        log { "players = $playersNames" }
        log { "defeated = $defeatedPlayers" }
        lastButNotDefeated(player).so { battleController.endBattle() }
        playersAndShips.remove(player)
        removeEnemy(player)
        if (playersNames.size == 1) {
            battleController.disconnect()
        }
    }
}

fun <E> List<E>.asIterator(): Iterator<E> {
    var count = 0
    return generateSequence { this[count++] }.iterator()
}