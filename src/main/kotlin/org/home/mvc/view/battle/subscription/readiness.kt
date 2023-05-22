package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent
import org.home.utils.log
import org.home.utils.logEvent

internal fun BattleView.playerIsReadyReceived() {
    val battleView = this@playerIsReadyReceived
    battleView.subscribe<PlayerIsReadyReceived> {
        logEvent(it, modelView)
        handleReady(it.player)
    }
}

internal fun BattleView.playerIsNotReadyReceived() {
    val battleView = this@playerIsNotReadyReceived
    battleView.subscribe<PlayerIsNotReadyReceived> {
        logEvent(it, modelView)
        handleNotReady(it.player)
    }
}

fun BattleView.readyPlayersReceived() {
    this.subscribe<ReadyPlayersReceived> { event ->
        logEvent(event, modelView)
        event.players.forEach {
            modelView.getReadyPlayers().add(it)
            handleReady(it)
        }
    }
}

private fun BattleView.handleReady(player: String) {
    modelView.log { "ready = $player" }
    handle(player, true, modelView::setReady) { fleet, readiness ->
        ready(player, fleet, readiness)
    }
}

private fun BattleView.handleNotReady(player: String) {
    modelView.log { "not ready = $player" }
    handle(player, false, modelView::setNotReady) { fleet, readiness ->
        notReady(player, fleet, readiness)
    }
}

private inline fun BattleView.handle(
    player: String,
    ready: Boolean,
    setReadiness: (String) -> Unit,
    setReadinessStyle: FleetGridStyleComponent.(FleetGrid, ShipsTypesPane) -> Unit,
) {
    setReadiness(player)
    readinessStyleComponent {
        setReadinessStyle(fleets(player), fleetsReadiness(player))
    }

    battleStartButtonController {
        battleStartButton.updateStyle(player, ready)
    }
}
