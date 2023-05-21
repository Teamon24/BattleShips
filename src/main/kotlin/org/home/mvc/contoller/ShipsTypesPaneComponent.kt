package org.home.mvc.contoller

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import org.home.mvc.GameComponent
import org.home.mvc.view.component.GridPaneExtensions.getCell
import org.home.mvc.view.component.GridPaneExtensions.removeColumn
import org.home.mvc.view.component.button.BattleButton
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.mvc.view.fleet.ShipsNumberLabel
import org.home.style.AppStyles.Companion.fullShipNumberLabel
import org.home.style.AppStyles.Companion.selectedCell
import org.home.style.AppStyles.Companion.shipTypeLabel
import org.home.utils.log
import tornadofx.action
import tornadofx.addClass
import tornadofx.onChange
import tornadofx.removeClass
import tornadofx.runLater

class ShipsTypesPaneComponent: GameComponent() {
    fun addShipTypeButton(gridPane: GridPane) {
        BattleButton("+").also {
            it.action {
                runLater {
                    val column = modelView.lastShipType() + 1
                    shipTypeLabel(gridPane, column)
                    shipsNumberLabel(gridPane, column, 1)

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

    fun shipTypeLabel(gridPane: GridPane, column: Int) =
        ShipTypeLabel(column)
            .addClass(shipTypeLabel, selectedCell)
            .also {
                gridPane.add(it, column, 0)
            }

    fun shipsNumberLabel(gridPane: GridPane, shipType: Int, shipsNumber: Int) =
        ShipsNumberLabel(shipsNumber).also {
            gridPane.add(it, shipType, 1)
            gridPane.minWidth = Region.USE_PREF_SIZE
        }

    fun shipsNumberLabel(gridPane: GridPane, shipType: Int, shipsNumber: SimpleIntegerProperty): FleetCellLabel {
        val fleetCellLabel = ShipsNumberLabel(shipsNumber.value)

        gridPane.add(fleetCellLabel, shipType, 1)
        gridPane.minWidth = Region.USE_PREF_SIZE

        val textProperty = fleetCellLabel.textProperty()

        fleetCellLabel {
            textProperty {
                shipsNumber.onChange { value = it.toString() }
                updateClass(value, shipType)

                onChange {
                    updateClass(it, shipType)
                }
            }
        }

        return fleetCellLabel
    }

    private fun FleetCellLabel.updateClass(it: String?, shipType: Int) {
        when {
            it == "0" -> addClass(fullShipNumberLabel)
            0 < it!!.toInt() && it.toInt() <= modelView.getShipsNumber(shipType) -> removeClass(fullShipNumberLabel)
            else -> throw RuntimeException("${FleetCellLabel::class.name} text can't accept value \"$it\"")
        }
    }

    private fun setNewNumberAsText(gridPane: GridPane, shipType: Int, newNumber: Int) {
        val cell = gridPane.getCell(1 to shipType) as Label
        cell.text = "$newNumber"
    }
}