package org.home.mvc.contoller

import org.home.net.Condition.Companion.condition

class AwaitConditions: AbstractGameController() {
    val fleetSettingsReceived = condition("fleet settings received", model)
    val newServerFound = condition("new server was found", model)
}