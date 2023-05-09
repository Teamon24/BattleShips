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
import org.home.app.AbstractApp.Companion.newGame
import org.home.mvc.ApplicationProperties.Companion.battleFieldCreationMenuTitle
import org.home.mvc.ApplicationProperties.Companion.createNewGameButtonText
import org.home.mvc.ApplicationProperties.Companion.heightFieldLabel
import org.home.mvc.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.ApplicationProperties.Companion.playersNumberLabel
import org.home.mvc.ApplicationProperties.Companion.squareSize
import org.home.mvc.ApplicationProperties.Companion.widthFieldLabel
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.view.AbstractGameView
import org.home.mvc.AppView
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.GridPaneExtensions.col
import org.home.mvc.view.components.GridPaneExtensions.marginGrid
import org.home.mvc.view.components.GridPaneExtensions.row
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.components.battleButton
import org.home.mvc.view.components.exitButton
import org.home.mvc.view.components.forwardSlide
import org.home.mvc.view.components.transferTo
import org.home.mvc.view.openAlertWindow
import org.home.net.message.Action
import org.home.style.AppStyles
import tornadofx.ChangeListener
import tornadofx.Form
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.label
import tornadofx.textfield
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class BattleCreationView : AbstractGameView("Настройки боя") {
    private val shipsTypesPaneController: ShipsTypesPaneController by newGame()
    private val battleController: BattleController<Action> by di()



    override val root = Form()
        .addClass(AppStyles.form)
        .apply { title = battleFieldCreationMenuTitle }

    private val ip = applicationProperties.ip
    private val freePort = applicationProperties.port
    private val ipAddress = SimpleStringProperty("$ip:$freePort")

    init {
        applicationProperties.isServer = true
        this.title = applicationProperties.currentPlayer.uppercase()
        with(root) {
            centerGrid {
                cell(0, 0) { settingsPane(ipAddress) }
                cell(1, 0) { shipsTypesPaneController.shipTypesPaneControl().also { add(it) } }
                cell(2, 0) {
                    marginGrid {
                        cell(0, 0) { backTransitButton<AppView>(this@BattleCreationView) }
                        cell(0, 1) { createBattleButton() }
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
                col(2) { exitButton(this@BattleCreationView) }
            }
        }
    }

    private fun EventTarget.createBattleButton() =
        battleButton(createNewGameButtonText) {
            action {
                try {
                    battleController.connect("", freePort)
                    this@BattleCreationView.transferTo<BattleView>()
                } catch (e: Exception) {
                    e.printStackTrace()
                    openAlertWindow {
                        "Не удалось создать хост ${ipAddress.value}"
                    }
                }
            }
        }

    private fun EventTarget.copyIpButton(ipAddress: SimpleStringProperty): Button {
        return battleButton("", ImageView("/icons/clipboard.png"))
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





