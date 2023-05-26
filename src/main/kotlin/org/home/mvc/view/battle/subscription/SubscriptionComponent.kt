package org.home.mvc.view.battle.subscription

import home.extensions.BooleansExtensions.otherwise
import org.home.app.ApplicationProperties
import org.home.mvc.GameComponent
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.model.invoke
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.openMessageWindow

class SubscriptionComponent: GameComponent() {
    fun BattleView.onPlayerTurn(event: TurnReceived) = onPlayerTurn(event.player)

    fun BattleView.onPlayerTurn(player: String) {
        modelView {
            turn.value = player.apply {
                isCurrent {
                    openMessageWindow { ApplicationProperties.yourTurnMessage }
                    enemiesViewController.enableAllExcept(getDefeatedPlayers())
                } otherwise {
                    enemiesViewController.disableAll()
                }
            }
        }
    }
}