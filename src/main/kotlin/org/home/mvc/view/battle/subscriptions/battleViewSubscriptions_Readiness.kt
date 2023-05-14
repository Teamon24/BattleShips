package org.home.mvc.view.battle.subscriptions

import home.extensions.AnysExtensions.invoke
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.view.battle.BattleView
import org.home.utils.log
import org.home.utils.logEvent

internal fun BattleView.playerIsReadyReceived() {
    val battleView = this@playerIsReadyReceived
    battleView.subscribe<PlayerIsReadyReceived> { event ->
        logEvent(event, model)
        model.log { "ready = $readyPlayers" }
    }
}

internal fun BattleView.playerIsNotReadyReceived() {
    val battleView = this@playerIsNotReadyReceived
    battleView.subscribe<PlayerIsNotReadyReceived> { event ->
        logEvent(event, model)
        model.log { "ready = $readyPlayers" }
    }
}

fun BattleView.readyPlayersReceived() {
    this.subscribe<ReadyPlayersReceived> { event ->
        logEvent(event, model)
        event.players.forEach {
            model.readyPlayers { if (it !in this) add(it) }
        }
    }
}