package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import org.home.mvc.AppView
import org.home.mvc.ApplicationProperties.Companion.connectionButtonText
import org.home.mvc.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.AbstractGameView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.backTransitButton
import org.home.mvc.view.component.button.battleButton
import org.home.mvc.view.component.transferTo
import org.home.mvc.view.openAlertWindow
import tornadofx.action
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : AbstractGameView("Присоединиться к битве") {
    private val battleClient: BattleController<Action> by di()

    private val ipAddress = SimpleStringProperty().apply {
        value = "${applicationProperties.ip}:${applicationProperties.port}"
    }

    private val currentView = this@BattleJoinView

    init {
        applicationProperties.isServer = false
        this.title = applicationProperties.currentPlayer.uppercase()
    }

    override val root = centerGrid {
        row(0) {
            col(0) { label(ipAddressFieldLabel) }
            col(1) { textfield(ipAddress) }
        }

        cell(1, 1) {
            battleButton(connectionButtonText) {
                action {
                    try {
                        val (ip, port) = extract()
                        battleClient.connect(ip, port)
                        currentView.transferTo<BattleView>()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        openAlertWindow {
                            "Не удалось подключиться к хосту ${ipAddress.value}"
                        }
                    }
                }
            }
        }

        cell(2, 1) { backTransitButton<AppView>(currentView) }
    }

    private fun extract(): Pair<String, Int> {
        val split = ipAddress.value.split(":")
        return split[0] to split[1].toInt()
    }
}



