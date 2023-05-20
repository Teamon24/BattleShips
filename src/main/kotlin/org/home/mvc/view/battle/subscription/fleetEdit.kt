package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.PlayerReadinessReceived
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.contoller.server.action.FleetEditAction
import org.home.mvc.contoller.server.action.NotReadyAction
import org.home.mvc.contoller.server.action.PlayerReadinessAction
import org.home.mvc.contoller.server.action.ReadyAction
import org.home.mvc.contoller.server.action.ShipAdditionAction
import org.home.mvc.contoller.server.action.ShipDeletionAction
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleView
import org.home.utils.logEvent

internal fun BattleView.shipWasAdded() {
    subscribe<ShipWasAdded> {
        processFleetEdit(
            it,
            ::ShipAdditionAction,
            { modelView, player, onTrue -> modelView.lastShipWasAdded(player, onTrue) },
            ::ReadyAction,
            ::PlayerIsReadyReceived
        )
    }
}

internal fun BattleView.shipWasDeleted() {
    subscribe<ShipWasDeleted> {
        processFleetEdit(
            it,
            ::ShipDeletionAction,
            { modelView, player, onTrue -> modelView.lastShipWasDeleted(player, onTrue) },
            ::NotReadyAction,
            ::PlayerIsNotReadyReceived
        )
    }
}

private fun BattleView.processFleetEdit(
    event: FleetEditEvent,
    action: (Int, String) -> FleetEditAction,
    lastShipWasEdited: (BattleModel, String, () -> Unit) -> Unit,
    createReadinessAction: (String) -> PlayerReadinessAction,
    createEvent: (String) -> PlayerReadinessReceived
) {
    logEvent(event, modelView)

    event {
        modelView {
            fleetsReadiness.update(event)
            battleController.send {
                player.isCurrent {
                    + action(shipType, currentPlayer)
                    lastShipWasEdited(modelView, player) {
                        eventbus(createEvent(player))
                        + createReadinessAction(player)
                    }
                }
            }
        }
    }
}
