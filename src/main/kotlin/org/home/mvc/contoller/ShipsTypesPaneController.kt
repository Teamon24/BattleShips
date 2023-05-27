package org.home.mvc.contoller

import home.extensions.AnysExtensions.invoke
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleMapProperty
import javafx.event.EventTarget
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import org.home.app.di.gameScope
import org.home.mvc.GameController
import org.home.mvc.model.invoke
import org.home.style.AppStyles.Companion.gridMargin
import tornadofx.add
import tornadofx.addClass

abstract class ShipsPaneController: GameController() {
    abstract fun shipsReadinessPane(player: String): ShipsPane
}

class ShipsTypesPaneController: ShipsPaneController() {
    private val component: ShipsTypesPaneComponent by gameScope()

    fun EventTarget.shipTypesPaneControl(): ShipsTypesPane {
        return ShipsTypesPane()
            .also {
                component.addShipTypeButton(it)
                component.removeShipTypeButton(it)
                it.addLabels(modelView.getShipsTypes())
                it.addClass(gridMargin)
                add(it)
            }
    }

    override fun shipsReadinessPane(player: String): ShipsPane {
        val shipsTypes = modelView.getFleetReadiness(player)
        return ShipsTypesPane()
            .apply { addLabels(shipsTypes) }
            .transposed()
            .apply { modelView { player.isNotCurrent { flip() } } }
    }

    private fun GridPane.addLabels(shipsTypes: SimpleMapProperty<Int, Int>) {
        for (shipType in shipsTypes) {
            component { addLabels(shipType.key, shipType.value) }
        }
    }

    private fun ShipsPane.addLabels(shipsTypes: Map<Int, SimpleIntegerProperty>) {
        for (shipType in shipsTypes) {
            component { addLabels(shipType.key, shipType.value) }
        }
    }
}

class FleetReadinessPaneController: ShipsPaneController() {

    private val fleetReadinessLabelController by gameScope<FleetReadinessLabelController>()

    override fun shipsReadinessPane(player: String) = FleetReadinessPane().apply {
        minWidth = Region.USE_PREF_SIZE
        modelView.getFleetReadiness(player).forEach { (type, numberProp) ->
            fleetReadinessLabelController.create(type, numberProp).also {
                labels[it] = null
                add(it, 0, type)
            }
        }
    }
}