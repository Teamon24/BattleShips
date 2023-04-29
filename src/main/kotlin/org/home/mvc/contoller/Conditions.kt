package org.home.mvc.contoller

import org.home.mvc.model.BattleModel
import org.home.net.Condition.Companion.condition
import tornadofx.Controller

class Conditions: Controller() {
    private val model: BattleModel by di()
    val fleetSettingsReceived = condition("fleet settings received", model)
    val newServerFound = condition("new server was found", model)
}