package org.home.mvc.view.component.button

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import org.home.app.ApplicationProperties.Companion.connectionButtonText
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.component.transferTo
import org.home.mvc.view.openAlertWindow
import tornadofx.action

class BattleConnectionButtonController: GameController() {
    private val battleClient: BattleController<Action> by di()

    val ipAddress = SimpleStringProperty().apply {
        value = "${applicationProperties.ip}:${applicationProperties.port}"
    }

    fun EventTarget.create(battleJoinView: BattleJoinView): BattleButton {
        return battleButton(connectionButtonText) {
            action {
                try {
                    val (ip, port) = extract()
                    battleClient.connect(ip, port)
                    battleJoinView.transferTo<BattleView>()
                } catch (e: Exception) {
                    e.printStackTrace()
                    openAlertWindow {
                        "Не удалось подключиться к хосту ${ipAddress.value}"
                    }
                }
            }
        }
    }

    private fun extract(): Pair<String, Int> {
        val split = ipAddress.value.split(":")
        return split[0] to split[1].toInt()
    }
}