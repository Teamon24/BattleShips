package org.home.mvc.view.battle.subscription

import home.extensions.CollectionsExtensions.excludeAll
import org.home.mvc.GameComponent
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.model.invoke
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.openMessageWindow
import org.home.utils.NodeUtils.disable
import org.home.utils.NodeUtils.enable
import org.home.utils.log

class SubscriptionComponent: GameComponent() {
    fun BattleView.playerTurn(event: TurnReceived) = playerTurn(event.player)

    fun BattleView.playerTurn(player: String) {
        modelView {
            turn.value = player
            if (currentPlayer == player) {
                openMessageWindow { "Ваш ход" }
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