package org.home.mvc.view.battle.subscriptions

import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.extensions.AnysExtensions.notIn
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.addClass

internal fun BattleView.playerWasDisconnected() {
    subscribeToRemove<PlayerWasDisconnected> {
        "${it.player} отключился"
    }
}

internal fun BattleView.playerLeaved() {
    subscribeToRemove<PlayerLeaved> {
        "${it.player} покинул ${model.battleIsEnded then "поле боя" or "бой"}"
    }
}

internal fun BattleView.playerWasDefeated() {
    subscribe<PlayerWasDefeated> { event ->
        model {

            logEvent(event, this)
            val defeated = event.player
            defeatedPlayers.add(defeated)

            val (fleetReadiness, fleetGrid) = when (currentPlayerIs(defeated)) {
                true -> currentPlayerFleetReadinessPane to (currentPlayerFleetGridPane.center as FleetGrid)
                else -> enemiesFleetsReadinessPanes[defeated]!! to enemiesFleetGridsPanes[defeated]!!.disable()
            }

            fleetGrid
                .addTitleCellClass(AppStyles.defeatedTitleCell)
                .onEachFleetCells {
                    it.coord
                        .notIn(getShots(defeated))
                        .so { it.addClass(AppStyles.defeatedCell) }
                }

            fleetReadiness
                .getTypeLabels()
                .forEach { it.addClass(AppStyles.defeatedTitleCell) }

            openMessageWindow {
                val args = when (currentPlayerIs(defeated)) {
                    true -> listOf("Вы", "и")
                    else -> listOf(defeated, "")
                }
                "${args[0]} проиграл${args[1]}"
            }

            currentPlayerIs(defeated).so { battleViewExitButton.text = "Покинуть поле боя" }
            hasAWinner().so { battleController.endBattle() }
            hasOnePlayerLeft().so { battleController.disconnect() }
        }
    }
}

private inline fun <reified T: HasAPlayer> BattleView.subscribeToRemove(
    crossinline function: (HasAPlayer) -> String
) {
    subscribe<T> {
        logEvent(it, model)
        removePlayer(it.player)
        openMessageWindow { function(it) }
    }
}

private fun BattleView.removePlayer(player: String) {
    model {
        log { "players = $playersNames" }
        log { "defeated = $defeatedPlayers" }

        lastButNotDefeated(player)
            .and(currentPlayer !in defeatedPlayers)
            .and(battleIsStarted)
            .so { battleController.endBattle() }

        playersAndShips.remove(player)
        removeEnemyFleet(player)
        if (playersNames.size == 1 && battleIsEnded) {
            battleController.disconnect()
        }
    }
}
