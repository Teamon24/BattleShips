package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.invoke
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.GridPane
import org.home.app.ApplicationProperties.Companion.heightFieldLabel
import org.home.app.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.app.ApplicationProperties.Companion.playersNumberLabel
import org.home.app.ApplicationProperties.Companion.squareSize
import org.home.app.ApplicationProperties.Companion.widthFieldLabel
import org.home.app.di.gameScope
import org.home.mvc.GameComponent
import org.home.mvc.model.BattleViewModel
import org.home.mvc.view.component.AddressComponent
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.marginGrid
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.button.battleButton
import tornadofx.ChangeListener
import tornadofx.action
import tornadofx.checkbox
import tornadofx.label
import tornadofx.textfield

class SettingsPaneController: GameComponent() {
    private val settingsFieldsController by gameScope<SettingsFieldsController>()
    private val addressComponent by gameScope<AddressComponent>()
    private val address by lazy { SimpleStringProperty(addressComponent.address()) }

    fun EventTarget.settingsPane(): GridPane {
        return marginGrid {
            row(0) {
                col(0) { label(ipAddressFieldLabel) }
                col(1) { textfield(address).apply { isEditable = false } }
                col(2) { copyIpButton(address) }
            }

            row(1) {
                col(0) { label(playersNumberLabel) }
                settingsFieldsController {
                    col(1) { playersField() }
                }
            }

            row(2) {
                col(0) { label(widthFieldLabel) }
                settingsFieldsController {
                    col(1) { widthField() }
                }
                col(2) { squareBattleFieldCheckBox() }
            }

            row(3) {
                col(0) { label(heightFieldLabel) }
                settingsFieldsController {
                    col(1) { heightField() }
                }
            }
        }
    }

    private fun EventTarget.copyIpButton(address: SimpleStringProperty): Button {
        return battleButton("", ImageView("/icons/clipboard.png")) {
            action {
                ClipboardContent().apply {
                    putString(address.value)
                    Clipboard.getSystemClipboard().setContent(this)
                }
            }
        }
    }


    private fun EventTarget.squareBattleFieldCheckBox() =
        checkbox(squareSize) {
            selectedProperty().set(true)
            action {
                isSelected { modelView.equalizeSizes() }
            }
            modelView.addSquareSizeListener(this)
        }

    private fun BattleViewModel.addSquareSizeListener(checkBox: CheckBox) {
        getWidth().addListener(changeListener(checkBox, ::setHeight, ::setWidth))
        getHeight().addListener(changeListener(checkBox, ::setWidth, ::setHeight))
    }

    private fun BattleViewModel.changeListener(
        checkBox: CheckBox,
        setterFirstSize: (BattleViewModel, Number?) -> Unit,
        setterSecondSize: (BattleViewModel, Number?) -> Unit
    ): ChangeListener<Number> {
        val battleModel = this@changeListener
        return ChangeListener { _, _, newValue ->
            checkBox.isSelected { setterFirstSize(battleModel, newValue) }
            setterSecondSize(battleModel, newValue)
        }
    }

    private fun setHeight(modelView: BattleViewModel, newValue: Number?) {
        modelView.getHeight().value = newValue as Int?
    }

    private fun setWidth(modelView: BattleViewModel, newValue: Number?) {
        modelView.getWidth().value = newValue as Int?
    }
}