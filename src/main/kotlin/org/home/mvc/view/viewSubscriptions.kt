package org.home.mvc.view

import javafx.scene.control.ListView
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.model.BattleModel
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.View

internal inline fun View.subscriptions(body: View.() -> Unit) {
    this.body()
}

internal inline fun View.refreshers(body: View.() -> Unit) {
    this.body()
}

fun View.playerWasDisconnected(model: BattleModel) {
    subscribe<PlayerWasDisconnected> {
        logEvent(it)
        model.playersAndShips.remove(it.player)
    }
}

fun View.readyPlayersReceived(model: BattleModel, playersListView: ListView<String>) {
    subscribe<ReadyPlayersReceived> { event ->
        logEvent(event)
        val playersReadiness = model.playersReadiness
        val players = event.readyPlayers

        playersReadiness {
            when {
                isEmpty() -> putAll(players.associateWith { true })
                else -> players.forEach { player -> put(player, true) }
            }
        }
    }
}

fun View.connectedPlayersReceived(model: BattleModel) {
    subscribe<ConnectedPlayersReceived> { event ->
        logEvent(event)
        event.players.forEach { player ->
            model.playersAndShips[player] = mutableListOf()
        }
    }
}

fun BattleModel.updateFleetsReadiness(event: FleetEditEvent) {
    val operation = event.operation
    fleetsReadiness[event.player]!![event.shipType]!!.operation()
}