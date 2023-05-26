package org.home.mvc.contoller

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleMapProperty
import javafx.event.EventTarget
import javafx.scene.layout.GridPane
import org.home.app.di.gameScope
import org.home.mvc.GameController
import tornadofx.add

class ShipsTypesPaneController: GameController() {
    private val component: ShipsTypesPaneComponent by gameScope()

    fun EventTarget.shipTypesPaneControl(): ShipsTypesPane {
        return ShipsTypesPane().apply {
            component.addShipTypeButton(this)
            component.removeShipTypeButton(this)
            addLabels(modelView.getShipsTypes())
        }.also {
            add(it)
        }
    }

    fun shipTypesPane(player: String): ShipsTypesPane {
        val shipsTypes = modelView.getFleetReadiness(player)
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
