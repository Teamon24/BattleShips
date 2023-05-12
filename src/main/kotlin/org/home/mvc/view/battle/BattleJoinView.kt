package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import org.home.app.AbstractApp.Companion.newGame
import org.home.mvc.ApplicationProperties.Companion.connectionButtonText
import org.home.mvc.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.view.AbstractGameView
import org.home.mvc.AppView
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.GridPaneExtensions.col
import org.home.mvc.view.components.GridPaneExtensions.row
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.components.battleButton
import org.home.mvc.view.components.forwardSlide
import org.home.mvc.view.components.transferTo
import org.home.mvc.view.openAlertWindow
import org.home.mvc.contoller.server.action.Action
import org.home.style.AppStyles
import tornadofx.action
import tornadofx.addClass
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : AbstractGameView("Присоединиться к битве") {
    private val battleClient: BattleController<Action> by di()
    private val awaitConditions: AwaitConditions by newGame()

    private val ipAddress = SimpleStringProperty().apply {
        value = "${applicationProperties.ip}:${applicationProperties.port}"
    }

    private val currentView = this@BattleJoinView

    init {
        applicationProperties.isServer = false
        this.title = applicationProperties.currentPlayer.uppercase()
    }

    override val root = centerGrid {
        addClass(AppStyles.form)
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
                        awaitConditions.fleetSettingsReceived.await()
                        currentView.transferTo<BattleView>(forwardSlide)
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



