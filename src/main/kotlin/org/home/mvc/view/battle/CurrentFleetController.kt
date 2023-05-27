package org.home.mvc.view.battle

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.home.app.di.gameScope
import org.home.mvc.GameController
import org.home.mvc.contoller.ShipsPaneController
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.style.AppStyles.Companion.currentPlayerLabel
import org.home.utils.NodeUtils.enable
import org.home.utils.StyleUtils.leftPadding
import tornadofx.add
import tornadofx.addClass

class CurrentFleetController: GameController() {
    private val fleetGridController by gameScope<FleetGridController>()
    private val shipsPaneController by gameScope<ShipsPaneController>()
    private val fleetReadinessPane =  shipsPaneController
        .shipsReadinessPane(currentPlayer)
        .apply { leftPadding(10) }
        .toBorderPane()

    private val fleetGridPane = fleetGridController
        .activeFleetGrid()
        .apply { addShips(modelView.shipsOf(currentPlayer)) }
        .toBorderPane()

    fun EventTarget.fleetGridPane(): BorderPane = fleetGridPane.also { add(it) }
    fun EventTarget.fleetReadinessPane(): BorderPane = fleetReadinessPane.also { add(it) }

    fun playerLabel() = Label(currentPlayer).addClass(currentPlayerLabel)

    private fun <T: Node> T.toBorderPane(): BorderPane {
        return BorderPane(this)
    }

    fun updateCurrentPlayerFleetGrid(): FleetGrid {
        return fleetGridController
            .fleetGrid()
            .addShips(modelView.shipsOf(currentPlayer))
            .also {
                fleetGridPane.center = it
            }
    }

    fun enableView() {
        fleetGridPane.enable()
        fleetReadinessPane.enable()
    }
}