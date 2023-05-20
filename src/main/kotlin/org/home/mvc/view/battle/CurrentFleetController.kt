package org.home.mvc.view.battle

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.home.app.di.GameScope
import org.home.mvc.contoller.GameController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.style.AppStyles
import org.home.utils.StyleUtils.leftPadding
import tornadofx.addClass

class CurrentFleetController: GameController() {
    private val fleetGridController by GameScope.inject<FleetGridController>()
    private val shipsTypesPaneController by GameScope.inject<ShipsTypesPaneController>()

    private val currentFleetPane = fleetGridController
        .activeFleetGrid()
        .apply { addShips(modelView.shipsOf(currentPlayer)) }
        .toBorderPane()

    fun fleetGrid() = currentFleetPane

    fun fleetReadinessPane(): BorderPane {
        return shipsTypesPaneController
            .shipTypesPane(currentPlayer)
            .transposed()
            .apply { leftPadding(10) }
            .toBorderPane()
    }

    fun playerLabel() = Label(currentPlayer).addClass(AppStyles.currentPlayerLabel)

    private fun <T: Node> T.toBorderPane(): BorderPane {
        return BorderPane(this)
    }

    fun updateCurrentPlayerFleetGrid(): FleetGrid {
        return fleetGridController
            .fleetGrid()
            .addShips(modelView.shipsOf(currentPlayer))
            .also {
                currentFleetPane.center = it
            }
    }
}