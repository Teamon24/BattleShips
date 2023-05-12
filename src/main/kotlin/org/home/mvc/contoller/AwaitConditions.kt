package org.home.mvc.contoller

import org.home.mvc.contoller.Condition.Companion.condition

class AwaitConditions: AbstractGameBean() {
    val fleetSettingsReceived = condition("fleet settings received", model)
    val newServerFound = condition("new server was found", model)
    val canContinueBattle = condition("battle can be continued", model)
}