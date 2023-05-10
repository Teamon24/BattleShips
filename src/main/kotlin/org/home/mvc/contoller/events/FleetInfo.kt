package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.FleetsReadinessAction

class FleetSettingsReceived(val settings: FleetSettingsAction): BattleEvent()
class FleetsReadinessReceived(action: FleetsReadinessAction): BattleEvent() {
    val states = action.states
}