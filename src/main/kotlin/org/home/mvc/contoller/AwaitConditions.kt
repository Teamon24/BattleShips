package org.home.mvc.contoller

import org.home.mvc.contoller.Condition.Companion.condition

class AwaitConditions: GameComponent() {
    val fleetSettingsReceived = condition("fleet settings received", modelView)
    val newServerFound = condition("new server was found", modelView)
    val canContinueBattle = condition("battle can be continued", modelView)
}