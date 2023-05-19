package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import home.extensions.CollectionsExtensions.containsOnly
import org.home.mvc.AppView
import org.home.mvc.ApplicationProperties.Companion.leaveBattleFieldButtonTransitionTime
import org.home.mvc.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.getIndices
import org.home.mvc.view.component.Transit
import org.home.mvc.view.component.button.BattleButton
import org.home.mvc.view.component.transferTo
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.StyleUtils.textFillTransition
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.action
import tornadofx.button
import tornadofx.style

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

            val fleetGrid = playersFleetGridsPanes[defeated]!!.disableIf(defeated.isNotCurrent)
            val fleetReadiness = playersFleetsReadinessPanes[defeated]!!

            defeatedStyleComponent {
                defeated(defeated, fleetGrid, fleetReadiness)
            }

            openMessageWindow {
                val args = defeated.isCurrent then listOf("Вы", "и") or listOf(defeated, "")
                "${args[0]} проиграл${args[1]}"
            }

            defeated.isCurrent {
                updateLeaveBattleFieldButton()
            }

            hasAWinner {
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

        playersAndShips.remove(player)
        hasAWinner().and(battleIsStarted).so {
            battleController.endBattle()
        }
        removeEnemyFleet(player)
        hasOnePlayerLeft().so { battleController.disconnect() }
    }
}

fun BattleView.updateLeaveBattleFieldButton() {
    (battleViewExitButton as BattleButton).disableHover()
    val buttonIndices = battleViewExitButtonIndices
    root {
        children.removeIf { it.getIndices() == buttonIndices }

        cell(buttonIndices.first, buttonIndices.second) {
            button(leaveBattleFieldText) {
                style {
                    filling(this@button) {
                        millis = leaveBattleFieldButtonTransitionTime
                        transition(initialAppColor, defeatedColor) { backgroundColor += it }
                        textFillTransition()
                    }
                }
                action {
                    battleController.onBattleViewExit()
                    transferTo<AppView>(Transit.BACKWARD)
                }
            }.also {
                battleViewExitButton = it
            }
        }
    }
}

internal fun BattleView.removeEnemyFleet(player: String) {
    playersFleetsReadinessPanes {
        remove(player)
        keys.containsOnly(currentPlayer).so { selectedEnemyFleetReadinessPane.center = null }
    }

    playersFleetGridsPanes {
        remove(player)
        keys.containsOnly(currentPlayer).so {
            selectedEnemyFleetPane.center = null
            selectedEnemyLabel.text = ""
        }
    }

    log { "removed from panes: $player" }
}
