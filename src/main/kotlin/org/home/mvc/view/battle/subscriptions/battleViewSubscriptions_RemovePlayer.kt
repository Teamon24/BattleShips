package org.home.mvc.view.battle.subscriptions

import home.extensions.AnysExtensions.invoke
import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles
import home.extensions.AnysExtensions.notIn
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import org.home.mvc.AppView
import org.home.mvc.ApplicationProperties
import org.home.mvc.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.mvc.view.components.BattleButton
import org.home.mvc.view.components.GridPaneExtensions
import org.home.mvc.view.components.GridPaneExtensions.getIndices
import org.home.mvc.view.components.backSlide
import org.home.mvc.view.components.transferTo
import org.home.style.AppStyles.Companion.defeatedTitleCell
import org.home.utils.logEvent
import tornadofx.action
import tornadofx.addClass
import tornadofx.button

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
        val battleView = this@playerWasDefeated
        model {

            logEvent(event, this)
            val defeated = event.player
            defeatedPlayers.add(defeated)

            val (fleetReadiness, fleetGrid) = when (currentPlayerIs(defeated)) {
                true -> currentPlayerFleetReadinessPane to
                        (currentPlayerFleetGridPane.center as FleetGrid)
                else -> enemiesFleetsReadinessPanes[defeated]!! to
                        enemiesFleetGridsPanes[defeated]!!.disable()
            }

            fleetGrid
                .addTitleCellClass(defeatedTitleCell)
                .onEachFleetCells {
                    it.coord
                        .notIn(getShots(defeated))
                        .so { it.addClass(AppStyles.defeatedCell) }
                }

            fleetReadiness
                .getTypeLabels()
                .forEach { it.addClass(defeatedTitleCell) }

            openMessageWindow {
                val args = when (currentPlayerIs(defeated)) {
                    true -> listOf("Вы", "и")
                    else -> listOf(defeated, "")
                }
                "${args[0]} проиграл${args[1]}"
            }

            currentPlayerIs(defeated).so {
                (battleViewExitButton as BattleButton).disableHover()
                battleView.updateLeaveBattleFieldButton()
            }

            hasAWinner().so {
                battleController.endBattle()
            }
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

        lastButNotDefeated(player)
            .and(currentPlayer !in defeatedPlayers)
            .and(battleIsStarted)
            .so {
                battleController.endBattle()
            }

        playersAndShips.remove(player)
        removeEnemyFleet(player)

        hasOnePlayerLeft().so { battleController.disconnect() }
    }
}

fun BattleView.updateLeaveBattleFieldButton() {

    this {
        val buttonIndices = battleViewExitButtonIndices
        root {
            children.removeIf { getIndices(it) == buttonIndices }

            GridPaneExtensions.cell(buttonIndices.first, buttonIndices.second) {
                button(leaveBattleFieldText) {
                    addClass(defeatedTitleCell)
                    action {
                        battleController.onBattleViewExit()
                        transferTo<AppView>(backSlide)
                    }
                }.also {
                    battleViewExitButton = it
                }
            }
        }
    }
}