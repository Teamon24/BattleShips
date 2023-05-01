package org.home.mvc.view.fleet

import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ShipWasConstructed
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.view.updateFleetsReadiness
import org.home.net.action.ShipDeletionAction
import org.home.net.action.ShipConstructionAction
import org.home.utils.logEvent

internal fun FleetGridCreationView.playerWasConnected() {
    subscribe<ConnectedPlayerReceived> { event ->
        val connectedPlayer = event.player
        val ships = model.playersAndShips[connectedPlayer]
        if (ships == null) {
            model.playersAndShips[connectedPlayer] = mutableListOf()
        }
        logEvent(event, model)
    }
}

internal fun FleetGridCreationView.playerIsReadyReceived() {
    subscribe<PlayerIsReadyReceived> { event ->
        logEvent(event, model)
    }
}

internal fun FleetGridCreationView.playerIsNotReadyReceived() {
    subscribe<PlayerIsNotReadyReceived> { event ->
        logEvent(event, model)
    }
}

internal fun FleetGridCreationView.shipWasConstructed() {
    subscribe<ShipWasConstructed> {
        if (it.player == currentPlayer) {
            val shipType = it.shipType
            model.fleetsReadiness[currentPlayer]!![shipType]!!.apply(it.operation)
            logEvent(it, model)
            battleController.send(ShipConstructionAction(shipType, currentPlayer))
        } else {
            model.updateFleetsReadiness(it)
        }
    }
}


internal fun FleetGridCreationView.shipWasDeleted() {
    subscribe<ShipWasDeleted> {
        if (it.player == currentPlayer) {
            val shipType = it.shipType
            model.fleetsReadiness[currentPlayer]!![shipType]!!.apply(it.operation)
            logEvent(it, model)
            battleController.send(ShipDeletionAction(shipType, currentPlayer))
        } else {
            model.updateFleetsReadiness(it)
        }
    }
}

internal fun FleetGridCreationView.fleetsReadinessReceived() {
    subscribe<FleetsReadinessReceived> { event ->
        val fleetsStates = model.fleetsReadiness
        val newFleetsStates = event.states
        newFleetsStates.forEach { (player, state) ->
            state.forEach { (shipType, number) ->
                fleetsStates[player] ?: run {
                    fleetsStates[player] = mutableMapOf()
                }

                fleetsStates[player]!![shipType] = SimpleIntegerProperty(number)
            }
        }
    }
}

