package org.home.mvc.view.battle

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region

import org.home.mvc.model.BattleModel
import org.home.mvc.view.components.getCell
import org.home.mvc.view.components.removeColumn
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.style.AppStyles
import org.home.utils.RomansDigits
import tornadofx.Controller
import tornadofx.action
import tornadofx.addClass
import tornadofx.runLater

class ShipTypePaneComponent: Controller() {

    private val model: BattleModel by di()

    private fun lastShipType() = model.battleShipsTypes.maxOfOrNull { entry -> entry.key } ?: 0

    fun addShipTypeButton(gridPane: GridPane) {
        Button("+").also {
            it.action {
                runLater {
                    val column = lastShipType() + 1
                    shipTypeLabel(gridPane, column)
                    shipsNumberLabel(gridPane, column, 1)

                    val shipsTypes = model.battleShipsTypes

                    shipsTypes.forEach { (shipType, number) ->
                        shipsTypes[shipType] = number + 1
                        setNewNumberAsText(gridPane, shipType, number + 1)
                    }
                    shipsTypes[column] = 1
                    println(shipsTypes)
                }
            }
            gridPane.add(it, 0, 0)
        }
    }

    fun removeShipTypeButton(gridPane: GridPane) {
        Button("-").also {
            it.action {
                runLater {
                    if ((lastShipType() != 0)) {
                        gridPane.removeColumn(lastShipType())
                        val shipsTypes = model.battleShipsTypes
                        shipsTypes.remove(lastShipType())
                        shipsTypes.forEach { (shipType, number) ->
                            val newNumber = number - 1
                            shipsTypes[shipType] = newNumber
                            setNewNumberAsText(gridPane, shipType, newNumber)
                        }
                        println(shipsTypes)
                    }
                }
            }

            gridPane.add(it, 0, 1)
        }

    }

    fun shipTypeLabel(gridPane: GridPane, column: Int) =
        FleetCellLabel(RomansDigits.arabicToRoman(column))
            .addClass(
                AppStyles.shipTypeLabel,
                AppStyles.chosenFleetCell
            )
            .also {
                gridPane.add(it, column, 0)
            }

    fun shipsNumberLabel(gridPane: GridPane, column: Int, value: Int) =
        FleetCellLabel("$value").also {
                gridPane.add(it, column, 1)
                gridPane.minWidth = Region.USE_PREF_SIZE
            }

    private fun setNewNumberAsText(gridPane: GridPane, shipType: Int, newNumber: Int) {
        val cell = gridPane.getCell(1 to shipType) as Label
        cell.text = "$newNumber"
    }
}