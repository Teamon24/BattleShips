package org.home.mvc.contoller

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleMapProperty
import javafx.scene.layout.GridPane
import org.home.mvc.model.BattleModel
import org.home.utils.extensions.BooleansExtensions.yes
import tornadofx.Controller

class ShipsTypesPaneController: AbstractGameController() {

    private val component: ShipTypePaneComponent by newGame()

    fun shipTypesPaneControl(): ShipsTypesPane {
        return ShipsTypesPane().apply {
                component.addShipTypeButton(this)
                component.removeShipTypeButton(this)
                addLabels(model.battleShipsTypes)
            }
    }

    fun shipTypesPane(player: String): ShipsTypesPane {
        val shipsTypes = model.fleetsReadiness[player]!!
        return ShipsTypesPane().apply { addLabels(shipsTypes) }
    }

    private fun GridPane.addLabels(shipsTypes: SimpleMapProperty<Int, Int>) {
        for (shipType in shipsTypes) {
            component.shipTypeLabel(this, shipType.key)
            component.shipsNumberLabel(this, shipType.key, shipType.value)
        }
    }

    private fun GridPane.addLabels(shipsTypes: Map<Int, SimpleIntegerProperty>) {
        for (shipType in shipsTypes) {
            component.shipTypeLabel(this, shipType.key)
            component.shipsNumberLabel(this, shipType.key, shipType.value)
        }
    }
}
