package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import org.home.ApplicationProperties
import org.home.ApplicationProperties.Companion.connectionButtonText
import org.home.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.contoller.events.FleetSettingsAccepted
import org.home.mvc.model.BattleModel
import org.home.mvc.view.AppView
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.row
import org.home.mvc.view.components.slide
import org.home.mvc.view.fleet.FleetGridCreationView
import org.home.mvc.view.openErrorWindow
import org.home.net.BattleClient
import org.home.net.BattleClient.Companion.fleetSettingsReceived
import org.home.net.Condition.Companion.waitFor
import org.home.net.ConnectAction
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : View("Присоединиться к битве") {

    private val applicationProperties: ApplicationProperties by di()
    private val model: BattleModel by di()
    private val battleClient: BattleClient by di()
    private val ipAddress = SimpleStringProperty().apply {
        value = "${applicationProperties.ip}:${applicationProperties.port}"
    }

    private val fleetGridCreationView = FleetGridCreationView::class
    private val currentView = this@BattleJoinView

    init {
        this.title = applicationProperties.currentPlayer.uppercase()
        subscribe<FleetSettingsAccepted> {
            model.put(it.settings)
        }
    }

    override val root = centerGrid {
        addClass(AppStyles.form)
        row(0) {
            col(0) { label(ipAddressFieldLabel).apply { addClass(AppStyles.fieldSize) } }
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

                        waitFor(fleetSettingsReceived) {
                            currentView.replaceWith(fleetGridCreationView, slide)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        openErrorWindow {
                            "Не удалось подключиться к хосту ${ipAddress.value}"
                        }
                    }
                }
            }
        }

        cell(2, 1) {
            backTransit(currentView, AppView::class)
        }
    }

    private fun connectMessage() = ConnectAction(applicationProperties.currentPlayer)

    private fun extract(): Pair<String, Int> {
        val split = ipAddress.value.split(":")
        return split[0] to split[1].toInt()
    }
}



