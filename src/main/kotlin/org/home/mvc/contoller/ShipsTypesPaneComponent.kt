package org.home.mvc.contoller

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.invoke
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.util.converter.NumberStringConverter
import org.home.mvc.GameComponent
import org.home.mvc.view.component.GridPaneExtensions.getCell
import org.home.mvc.view.component.GridPaneExtensions.removeColumn
import org.home.mvc.view.component.button.BattleButton
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.mvc.view.fleet.ShipsNumberLabel
import org.home.style.AppStyles.Companion.fleetCell
import org.home.style.AppStyles.Companion.fullShipNumberLabel
import org.home.style.AppStyles.Companion.selectedCell
import org.home.utils.StyleUtils.toggle
import org.home.utils.log
import tornadofx.action
import tornadofx.addClass
import tornadofx.onChange
import tornadofx.runLater

class ShipsTypesPaneComponent: GameComponent() {
    fun addShipTypeButton(gridPane: GridPane) {
        BattleButton("+").also {
            it.action {
                runLater {
                    val column = modelView.lastShipType() + 1
                    gridPane.addLabels(column, 1)

                    val shipsTypes = modelView.getShipsTypes()

                    shipsTypes.forEach { (shipType, number) ->
                        shipsTypes[shipType] = number + 1
                        setNewNumberAsText(gridPane, shipType, number + 1)
                    }
                    shipsTypes[column] = 1
                    log { "ships types:" }
                    log { shipsTypes }
                }
            }
            gridPane.add(it, 0, 0)
        }
    }

    fun removeShipTypeButton(gridPane: GridPane) {
        BattleButton("-").also {
            it.action {
                runLater {
                    val lastShipType = modelView.lastShipType()
                    if (lastShipType != 0) {
                        gridPane.removeColumn(lastShipType)
                        val shipsTypes = modelView.getShipsTypes()
                        shipsTypes.remove(lastShipType)
                        shipsTypes.forEach { (shipType, number) ->
                            val newNumber = number - 1
                            shipsTypes[shipType] = newNumber
                            setNewNumberAsText(gridPane, shipType, newNumber)
                        }
                        log { "ships types:" }
                        log { shipsTypes }
                    }
                }
            }
            gridPane.add(it, 0, 1)
        }
    }

    fun GridPane.addLabels(shipType: Int, shipsNumber: Int) {
        minWidth = Region.USE_PREF_SIZE
        ShipTypeLabel(shipType).also { add(it, shipType, 0) }.addClass(selectedCell)
        ShipsNumberLabel(shipsNumber).also { add(it, shipType, 1) }
    }

    fun ShipsPane.addLabels(shipType: Int, shipsNumber: SimpleIntegerProperty) {
        minWidth = Region.USE_PREF_SIZE
        val shipTypeLabel = ShipTypeLabel(shipType)
        val shipsNumberLabel = shipsNumberLabel(shipType, shipsNumber)

        shipTypeLabel.also { add(it, shipType, 0) }.addClass(selectedCell)
        shipsNumberLabel.also { add(it, shipType, 1) }

        labels[shipTypeLabel] = shipsNumberLabel
    }

    private fun shipsNumberLabel(shipType: Int, shipsNumber: SimpleIntegerProperty): ShipsNumberLabel {
        val shipsNumberLabel = ShipsNumberLabel(shipsNumber.value)
        shipsNumberLabel.textProperty().bindBidirectional(shipsNumber, NumberStringConverter())
        shipsNumberLabel.updateClass(shipType)
        shipsNumberLabel { textProperty().onChange { updateClass(shipType) } }
        return shipsNumberLabel
    }

    private fun ShipsNumberLabel.updateClass(shipType: Int) {
        val value = text.toInt()
        when {
            value == 0 -> addClass(fullShipNumberLabel)
            0 < value && value <= modelView.getShipsNumber(shipType) -> toggle(fullShipNumberLabel, fleetCell)
            else -> throw RuntimeException("${this.className} text can't accept value \"$text\"")
        }
    }

    private fun setNewNumberAsText(gridPane: GridPane, shipType: Int, newNumber: Int) {
        val cell = gridPane.getCell(1 to shipType) as Label
        cell.text = "$newNumber"
    }
}