package org.home.mvc.view.battle.subscription

import home.extensions.CollectionsExtensions.excludeAll
import org.home.app.ApplicationProperties
import org.home.mvc.GameComponent
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.model.invoke
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.openMessageWindow
import org.home.utils.NodeUtils.disable
import org.home.utils.NodeUtils.enable
import org.home.utils.log

class SubscriptionComponent: GameComponent() {
    fun BattleView.onPlayerTurn(event: TurnReceived) = onPlayerTurn(event.player)

    fun BattleView.onPlayerTurn(player: String) {
        modelView {
            turn.value = player
            if (currentPlayer == player) {
                openMessageWindow { ApplicationProperties.yourTurnMessage }
                log { "defeated = ${getDefeatedPlayers()}" }
                modelView {
                    enemiesFleetGridsPanes.excludeAll(getDefeatedPlayers()).enable()
                    enemiesFleetsReadinessPanes.excludeAll(getDefeatedPlayers()).enable()
                }
            } else {
                enemiesFleetGridsPanes.disable()
                enemiesFleetsReadinessPanes.disable()
            }
        }
    }
}