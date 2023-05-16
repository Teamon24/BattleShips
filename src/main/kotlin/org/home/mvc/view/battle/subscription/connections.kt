package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.view.battle.BattleView
import org.home.style.StyleUtils.rightPadding
import org.home.utils.logEvent

internal fun BattleView.playerWasConnected() {
    subscribe<ConnectedPlayerReceived> {
        logEvent(it, model)
        addNewFleet(it.player)
    }
}

internal fun BattleView.connectedPlayersReceived() {
    subscribe<ConnectedPlayersReceived> { event ->
        logEvent(event, model)
        event.players.forEach { connected -> addNewFleet(connected) }
    }
}

internal fun BattleView.fleetsReadinessReceived() {
    subscribe<FleetsReadinessReceived> { event ->
        logEvent(event, model)
        model.fleetsReadiness {
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

internal fun BattleView.addNewFleet(connectedPlayer: String) {
    model.initShips(connectedPlayer)
    addSelectedPlayer(connectedPlayer)
    addEnemyFleetGrid(connectedPlayer)
    addEnemyFleetReadinessPane(connectedPlayer)
}

private fun BattleView.addSelectedPlayer(player: String) {
    enemiesView.getSelectedEnemyLabel().apply {
        text.ifBlank { text = player }
    }
}

private fun BattleView.addEnemyFleetGrid(enemy: String) {
    val fleetGrid = enemyFleetGrid()
    playersFleetGridsPanes[enemy] = fleetGrid.disable()
    selectedEnemyFleetPane.center ?: run { selectedEnemyFleetPane.center = fleetGrid }
}

private fun BattleView.addEnemyFleetReadinessPane(enemy: String) {
    val readinessPane = enemyFleetReadinessPane(enemy)
    playersFleetsReadinessPanes[enemy] = readinessPane.disable()
    selectedEnemyFleetReadinessPane {
        center ?: run { center = readinessPane }
    }
}

private fun BattleView.enemyFleetReadinessPane(player: String) =
    shipsTypesPaneController.shipTypesPane(player).transposed().flip().apply { rightPadding(10) }