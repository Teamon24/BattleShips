package org.home.mvc.contoller

import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import org.home.mvc.view.components.BattleButton
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.components.GridPaneExtensions.removeColumn
import org.home.mvc.view.fleet.FleetCellLabel
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.mvc.view.fleet.ShipsNumberLabel
import org.home.style.AppStyles
import home.extensions.AnysExtensions.name
import org.home.utils.log
import tornadofx.action
import tornadofx.addClass
import tornadofx.onChange
import tornadofx.runLater

class ShipTypePaneComponent: AbstractGameBean() {
    private fun lastShipType() = model.battleShipsTypes.maxOfOrNull { entry -> entry.key } ?: 0

    fun addShipTypeButton(gridPane: GridPane) {
        BattleButton("+").also {
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
                    if ((lastShipType() != 0)) {
                        gridPane.removeColumn(lastShipType())
                        val shipsTypes = model.battleShipsTypes
                        shipsTypes.remove(lastShipType())
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
            .addClass(
                AppStyles.shipTypeLabel,
                AppStyles.chosenCell
            )
            .also {
                gridPane.add(it, column, 0)
            }

    fun shipsNumberLabel(gridPane: GridPane, shipType: Int, shipsNumber: Int) =
        ShipsNumberLabel(shipsNumber).also {
            gridPane.add(it, shipType, 1)
            gridPane.minWidth = Region.USE_PREF_SIZE
        }

    fun shipsNumberLabel(gridPane: GridPane, shipType: Int, shipsNumber: SimpleIntegerProperty): FleetCellLabel {
        return FleetCellLabel(shipsNumber.value.toString()).apply {
            val textProperty = textProperty()
            shipsNumber.onChange { textProperty.value = it.toString() }
            gridPane.add(this, shipType, 1)
            gridPane.minWidth = Region.USE_PREF_SIZE
            textProperty.onChange {
                when {
                    it == "0" -> addClass(AppStyles.titleCell)
                    0 < it!!.toInt() && it.toInt() <= model.battleShipsTypes[shipType]!! -> removeAnyColor()
                    else -> throw RuntimeException("${FleetCellLabel::class.name} text can't accept value \"$it\"")
                }
            }
        }
    }

    private fun setNewNumberAsText(gridPane: GridPane, shipType: Int, newNumber: Int) {
        val cell = gridPane.getCell(1 to shipType) as Label
        cell.text = "$newNumber"
    }
}