package org.home.mvc.contoller

import javafx.beans.property.SimpleMapProperty
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import org.home.mvc.contoller.events.ShipCountEvent
import org.home.mvc.contoller.events.ShipDiscountEvent
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.ShipTypePaneComponent
import org.home.mvc.view.components.getCell
import org.home.mvc.view.components.marginGrid
import org.home.mvc.view.components.transpose
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.style.AppStyles
import tornadofx.Controller
import tornadofx.addClass

class ShipsTypesPaneController: Controller() {

    private val model: BattleModel by di()
    private val component: ShipTypePaneComponent by di()

    fun shipTypesPaneControl(): GridPane {
        val shipsTypes = model.battleShipsTypes

        return marginGrid().apply {
            component.addShipTypeButton(this)
            component.removeShipTypeButton(this)
            addLabels(shipsTypes)
        }
    }

    fun shipTypesPane(): GridPane {
        val shipsTypes = model.battleShipsTypes
        return marginGrid().apply {
            addLabels(shipsTypes)
            addShipCountEventListener()
            addShipDiscountEventListener()
        }.transpose()
    }

    private fun GridPane.addLabels(shipsTypes: SimpleMapProperty<Int, Int>) {
        for (shipType in shipsTypes) {
            component.shipTypeLabel(this, shipType.key)
            component.shipsNumberLabel(this, shipType.key, shipType.value)
        }
    }

    private fun GridPane.addShipDiscountEventListener() {
        subscribe<ShipDiscountEvent> {
            val shipsNumber = getCell(it.type, 1) as Label
            shipsNumber.text = "${shipsNumber.text.toInt() + 1}"
        }
    }

    private fun GridPane.addShipCountEventListener() {
        subscribe<ShipCountEvent> {
            val shipsNumberLabel = getCell(it.type, 1) as Label
            val rest = shipsNumberLabel.text.toInt() - 1
            shipsNumberLabel.text = "$rest"
            if (rest == 0) {
                shipsNumberLabel.addClass(AppStyles.titleCell)
            } else {
                shipsNumberLabel.removeAnyColor()
            }
        }
    }
}
