package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.view.battle.BattleView
import org.home.utils.logEvent

internal fun BattleView.playerWasConnected() {
    subscribe<ConnectedPlayerReceived> {
        logEvent(it, modelView)
        enemiesView.add(it.player)
    }
}

internal fun BattleView.connectedPlayersReceived() {
    subscribe<ConnectedPlayersReceived> { event ->
        logEvent(event, modelView)
        event.players.forEach { connected -> enemiesView.add(connected) }
    }
}

internal fun BattleView.fleetsReadinessReceived() {
    subscribe<FleetsReadinessReceived> { event ->
        logEvent(event, modelView)
        modelView.fleetsReadiness {
            event.states.forEach { (player, state) ->
                get(player) ?: run { put(player, mutableMapOf()) }

                state.forEach { (shipType, number) ->
                    val shipNumberProperty = get(player)!![shipType]
                    shipNumberProperty ?: run {
                        get(player)!![shipType] = SimpleIntegerProperty(number)
                        return@fleetsReadiness
                    }

                    shipNumberProperty.value = number
                }
            }
        }
    }
}

