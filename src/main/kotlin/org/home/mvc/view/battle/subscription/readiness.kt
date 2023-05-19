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
        logEvent(it, model)
        handleReady(it.player)
    }
}

internal fun BattleView.playerIsNotReadyReceived() {
    val battleView = this@playerIsNotReadyReceived
    battleView.subscribe<PlayerIsNotReadyReceived> {
        logEvent(it, model)
        handleNotReady(it.player)
    }
}

fun BattleView.readyPlayersReceived() {
    this.subscribe<ReadyPlayersReceived> { event ->
        logEvent(event, model)
        event.players.forEach {
            model.readyPlayers.add(it)
            handleReady(it)
        }
    }
}

private fun BattleView.handleReady(player: String) {
    model.log { "ready = $readyPlayers" }
    handle(player, true, model::setReady) { fleet, readiness ->
        ready(player, fleet, readiness)
    }
}

private fun BattleView.handleNotReady(player: String) {
    model.log { "not ready = $readyPlayers" }
    handle(player, false, model::setNotReady) { fleet, readiness ->
        notReady(player, fleet, readiness)
    }
}

private inline fun BattleView.handle(
    player: String,
    ready: Boolean,
    setReadiness: (String) -> Unit,
    fleetStyleReadiness: FleetGridStyleComponent.(FleetGrid, ShipsTypesPane) -> Unit,
) {
    setReadiness(player)
    readinessStyleComponent {
        fleetStyleReadiness(
            playersFleetGridsPanes[player]!!,
            playersFleetsReadinessPanes[player]!!
        )
    }

    startButtonController {
        battleStartButton.updateStyle(player, ready)
    }
}
