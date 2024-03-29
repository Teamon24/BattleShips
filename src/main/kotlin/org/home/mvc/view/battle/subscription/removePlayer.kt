package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.component.GridPaneExtensions.getIndices
import org.home.mvc.view.openMessageWindow
import org.home.utils.NodeUtils.disableIf
import org.home.utils.log
import org.home.utils.logEvent

internal fun BattleView.playerWasDisconnected() {
    subscribeToRemove<PlayerWasDisconnected> {
        "${it.player} отключился"
    }
}

internal fun BattleView.playerLeaved() {
    subscribeToRemove<PlayerLeaved> {
        "${it.player} покинул ${modelView.battleIsEnded() then "поле боя" or "бой"}"
    }
}

internal fun BattleView.playerWasDefeated() {
    subscribe<PlayerWasDefeated> { event ->
        modelView {

            logEvent(event, this)
            val defeated = event.player
            getDefeatedPlayers().add(defeated)

            val fleetGrid = fleets(defeated).disableIf(defeated.isNotCurrent)
            val fleetReadiness = fleetsReadiness(defeated)

            defeatedStyleComponent {
                log { "${defeatedStyleComponent.name}#defeated" }
                defeated(defeated, fleetGrid, fleetReadiness)
            }

            defeated.isCurrent {
                battleViewExitButtonController {
                    root.children.removeIf { it.getIndices() == indices }
                    setToCell(currentView().setDefeated(root))
                }
            }

            openMessageWindow {
                val args = defeated.isCurrent then listOf("Вы", "и") or listOf(defeated, "")
                "${args[0]} проиграл${args[1]}"
            }

            hasAWinner {
                battleController.endBattle()
            }
        }
    }
}


private inline fun <reified T: HasAPlayer> BattleView.subscribeToRemove(
    crossinline message: (HasAPlayer) -> String
) {
    subscribe<T> {
        logEvent(it, modelView)
        removePlayer(it.player)
        openMessageWindow { message(it) }
    }
}

private fun BattleView.removePlayer(player: String) {
    modelView {
        enemiesViewController.remove(player)
        battleIsStarted().not().so {
            battleStartButtonController.updateStyle(player, false)
        }

        hasAWinner().and(battleIsStarted()).so {
            battleController.endBattle()
        }

        hasOnePlayerLeft().so {
            battleController.disconnect()
        }
    }
}


