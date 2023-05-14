package org.home.mvc.view.battle.subscriptions

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.so
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.server.action.FleetEditAction
import org.home.mvc.contoller.server.action.NotReadyAction
import org.home.mvc.contoller.server.action.PlayerReadinessAction
import org.home.mvc.contoller.server.action.ReadyAction
import org.home.mvc.contoller.server.action.ShipAdditionAction
import org.home.mvc.contoller.server.action.ShipDeletionAction
import org.home.mvc.view.battle.BattleView
import org.home.utils.logEvent

internal fun BattleView.shipWasAdded() {
    subscribe<ShipWasAdded> {
        processFleetEdit(it, ::ShipAdditionAction, model::setReady, ::ReadyAction)
    }
}

internal fun BattleView.shipWasDeleted() {
    subscribe<ShipWasDeleted> {
        processFleetEdit(it, ::ShipDeletionAction, model::setNotReady, ::NotReadyAction)
    }
}

private fun BattleView.processFleetEdit(
    event: FleetEditEvent,
    action: (Int, String) -> FleetEditAction,
    setReadiness: (String) -> Unit,
    createReadinessAction: (String) -> PlayerReadinessAction
) {
    logEvent(event, model)

    event {
        model {
            updateFleetReadiness(event)
            battleController.send {
                player.isCurrent {
                    + action(shipType, currentPlayer)
                    player.addedAllShips {
                        setReadiness(player)
                        + createReadinessAction(player)
                    }
                }
            }
        }
    }
}
