package org.home.mvc.view.battle

import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.GridPane
import org.home.app.ApplicationProperties
import org.home.app.DiApp
import org.home.app.ApplicationProperties.Companion.battleFieldCreationMenuTitle
import org.home.app.ApplicationProperties.Companion.heightFieldLabel
import org.home.app.injecting
import org.home.app.ApplicationProperties.Companion.ipFieldLabel
import org.home.app.ApplicationProperties.Companion.playersNumberLabel
import org.home.app.ApplicationProperties.Companion.squareSize
import org.home.app.ApplicationProperties.Companion.widthFieldLabel
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.model.BattleModel
import org.home.mvc.view.AppView
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.marginGrid
import org.home.mvc.view.components.row
import org.home.mvc.view.components.transit
import org.home.mvc.view.fleet.FleetCreationView
import org.home.style.AppStyles
import org.home.utils.IpUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import tornadofx.ChangeListener
import tornadofx.Form
import tornadofx.UIComponent
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.label
import tornadofx.launch
import tornadofx.required
import tornadofx.textfield

class BattleCreationView : View("Настройки боя"), KoinComponent {

    private val model: BattleModel by injecting()
    private val applicationProperties: ApplicationProperties by injecting()
    private val shipsTypesPaneController: ShipsTypesPaneController by injecting()
    private val fleetCreationView = this.get<FleetCreationView>()

    override val root = Form()
        .addClass(AppStyles.form)
        .apply { title = battleFieldCreationMenuTitle }

    init {

        with(root) {
            centerGrid {
                cell(0, 0) { settingsPane() }
                cell(1, 0) { shipsTypesPaneController.shipTypesPaneControl().also { add(it) } }
                cell(2, 0) {
                    marginGrid {
                        val view = this@BattleCreationView
                        cell(0, 0) { backTransit(view, AppView::class) }
                        cell(0, 1) {
                            transit(view, fleetCreationView, "Создать") {
                                applicationProperties["isServer"] = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun EventTarget.settingsPane(): GridPane {

        return marginGrid {
            row(0) {
                col(0) { fieldLabel(ipFieldLabel) }
                col(1) { textfield(IpUtils.publicIp()).apply { isEditable = false } }
                col(2) { copyIpButton() }
            }

            row(1) {
                col(0) { fieldLabel(playersNumberLabel) }
                col(1) { intField(model.playersNumber) }
            }

            row(2) {
                col(0) { fieldLabel(widthFieldLabel) }
                col(1) { intField(model.fleetGridWidth) }
                col(2) { squareBattleFieldCheckBox().also { add(it) } }
            }

            row(3) {
                col(0) { fieldLabel(heightFieldLabel) }
                col(1) { intField(model.fleetGridHeight) }
            }
        }
    }

    private fun EventTarget.fieldLabel(text: String): Label {
        return label(text).apply { addClass(AppStyles.fieldSize) }
    }

    private fun EventTarget.copyIpButton(): Button {
        return button("", ImageView("/icons/clipboard.png"))
            .apply {
                action {
                    ClipboardContent().apply {
                        putString(IpUtils.publicIp())
                        Clipboard.getSystemClipboard().setContent(this)
                    }
                }
            }
    }


    private fun EventTarget.intField(prop: SimpleIntegerProperty): TextField {
        return textfield() {
            bind(prop)
            required()
            addClass(AppStyles.fieldSize)
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

        model.fleetGridWidth.addListener(
            ChangeListener { _, _, newValue ->
                if (checkBox.isSelected) {
                    model.fleetGridHeight.value = newValue as Int?
                }
                model.fleetGridWidth.value = newValue as Int?
            })

        model.fleetGridHeight.addListener(ChangeListener { _, _, newValue ->
            if (checkBox.isSelected) {
                model.fleetGridWidth.value = newValue as Int?
            }
            model.fleetGridHeight.value = newValue as Int?
        })

        return checkBox
    }



    companion object {
        class BCMApp : DiApp<BattleCreationView>(BattleCreationView::class)

        @JvmStatic
        fun main(args: Array<String>) {
            org.home.startKoin("server")
            launch<BCMApp>()
        }
    }
}





