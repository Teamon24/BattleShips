package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import org.home.mvc.ApplicationProperties
import org.home.mvc.ApplicationProperties.Companion.connectionButtonText
import org.home.mvc.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.contoller.Conditions
import org.home.mvc.view.app.AppView
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.GridPaneExtensions.col
import org.home.mvc.view.components.GridPaneExtensions.row
import org.home.mvc.view.components.slide
import org.home.mvc.view.fleet.FleetGridCreationView
import org.home.mvc.view.openAlertWindow
import org.home.net.BattleClient
import org.home.net.action.PlayerConnectionAction
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : View("Присоединиться к битве") {

    private val applicationProperties: ApplicationProperties by di()
    private val battleClient: BattleClient by di()
    private val conditions: Conditions by di()
    private val ipAddress = SimpleStringProperty().apply {
        value = "${applicationProperties.ip}:${applicationProperties.port}"
    }

    private val fleetGridCreationView = FleetGridCreationView::class
    private val currentView = this@BattleJoinView

    init {
        this.title = applicationProperties.currentPlayer.uppercase()
    }

    override val root = centerGrid {
        addClass(AppStyles.form)
        row(0) {
            col(0) { label(ipAddressFieldLabel) }
            col(1) { textfield(ipAddress) }
        }

        cell(1, 1) {
            button(connectionButtonText) {
                action {
                    try {
                        applicationProperties.isServer = false
                        val (ip, port) = extract()
                        battleClient.connect(ip, port)
                        battleClient.listen()
                        battleClient.send(connectMessage())
                        conditions.fleetSettingsReceived.await()
                        currentView.replaceWith(fleetGridCreationView, slide)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        openAlertWindow {
                            "Не удалось подключиться к хосту ${ipAddress.value}"
                        }
                    }
                }
            }
        }

        cell(2, 1) {
            backTransitButton(currentView, AppView::class)
        }
    }

    private fun connectMessage() = PlayerConnectionAction(applicationProperties.currentPlayer)

    private fun extract(): Pair<String, Int> {
        val split = ipAddress.value.split(":")
        return split[0] to split[1].toInt()
    }
}



