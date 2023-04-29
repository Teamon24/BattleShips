package org.home.mvc.view.battle

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.GridPane
import org.home.mvc.ApplicationProperties
import org.home.mvc.ApplicationProperties.Companion.battleFieldCreationMenuTitle
import org.home.mvc.ApplicationProperties.Companion.heightFieldLabel
import org.home.mvc.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.ApplicationProperties.Companion.playersNumberLabel
import org.home.mvc.ApplicationProperties.Companion.squareSize
import org.home.mvc.ApplicationProperties.Companion.widthFieldLabel
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.model.BattleModel
import org.home.mvc.view.AppView
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.marginGrid
import org.home.mvc.view.components.row
import org.home.mvc.view.components.slide
import org.home.mvc.view.fleet.FleetGridCreationView
import org.home.mvc.view.openAlertWindow
import org.home.net.server.BattleServer
import org.home.style.AppStyles
import tornadofx.ChangeListener
import tornadofx.Form
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.label
import tornadofx.textfield

class BattleCreationView : View("Настройки боя") {

    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()
    private val shipsTypesPaneController: ShipsTypesPaneController by di()
    private val battleServer: BattleServer by di()

    override val root = Form()
        .addClass(AppStyles.form)
        .apply { title = battleFieldCreationMenuTitle }

    private val ip = applicationProperties.ip
    private val freePort = applicationProperties.port
    private val ipAddress = SimpleStringProperty("$ip:$freePort")

    init {
        this.title = applicationProperties.currentPlayer.uppercase()
        with(root) {
            centerGrid {
                cell(0, 0) { settingsPane(ipAddress) }
                cell(1, 0) { shipsTypesPaneController.shipTypesPaneControl().also { add(it) } }
                cell(2, 0) {
                    marginGrid {
                        val view = this@BattleCreationView
                        cell(0, 0) { backTransitButton(view, AppView::class) }
                        cell(0, 1) { createBattleButton(view) }
                    }
                }
            }
        }
    }


    private fun EventTarget.settingsPane(ipAddress: SimpleStringProperty): GridPane {

        return marginGrid {
            row(0) {
                col(0) { label(ipAddressFieldLabel) }
                col(1) { textfield(ipAddress).apply { isEditable = false } }
                col(2) { copyIpButton(ipAddress) }
            }

            row(1) {
                col(0) { label(playersNumberLabel) }
                col(1) { intField(model.playersNumber) }
            }

            row(2) {
                col(0) { label(widthFieldLabel) }
                col(1) { intField(model.width) }
                col(2) { squareBattleFieldCheckBox().also { add(it) } }
            }

            row(3) {
                col(0) { label(heightFieldLabel) }
                col(1) { intField(model.height) }
            }
        }
    }

    private fun EventTarget.createBattleButton(view: BattleCreationView) =
        button("Создать") {
            action {
                try {
                    applicationProperties.isServer = true
                    battleServer.start(freePort)
                    view.replaceWith(FleetGridCreationView::class, slide)
                } catch (e: Exception) {
                    e.printStackTrace()
                    openAlertWindow {
                        "Не удалось создать хост ${ipAddress.value}"
                    }
                }
            }
        }

    private fun EventTarget.copyIpButton(ipAddress: SimpleStringProperty): Button {
        return button("", ImageView("/icons/clipboard.png"))
            .apply {
                action {
                    ClipboardContent().apply {
                        putString(ipAddress.value)
                        Clipboard.getSystemClipboard().setContent(this)
                    }
                }
            }
    }


    private fun EventTarget.intField(prop: SimpleIntegerProperty): TextField {
        return textfield(prop) {
            focusedProperty().addListener { _, _, _ ->
                if (!text.matches(Regex("\\d+"))) {
                    text = ""
                }
            }
        }
    }

    private fun squareBattleFieldCheckBox(): CheckBox {

        val checkBox = CheckBox(squareSize).apply {
            selectedProperty().set(true)
            action {
                if (isSelected) { model.equalizeSizes() }
            }
        }

        model.width.addListener(
            ChangeListener { _, _, newValue ->
                if (checkBox.isSelected) {
                    model.height.value = newValue as Int?
                }
                model.width.value = newValue as Int?
            })

        model.height.addListener(ChangeListener { _, _, newValue ->
            if (checkBox.isSelected) {
                model.width.value = newValue as Int?
            }
            model.height.value = newValue as Int?
        })

        return checkBox
    }
}





