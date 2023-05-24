package org.home.mvc.view.battle

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.home.app.di.gameScope
import org.home.mvc.GameController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.style.AppStyles.Companion.currentPlayerLabel
import org.home.utils.NodeUtils.enable
import org.home.utils.StyleUtils.leftPadding
import tornadofx.addClass

class CurrentFleetController: GameController() {
    private val fleetGridController by gameScope<FleetGridController>()
    private val shipsTypesPaneController by gameScope<ShipsTypesPaneController>()

    private val fleetReadinessPane =  shipsTypesPaneController
        .shipTypesPane(currentPlayer)
        .transposed()
        .apply { leftPadding(10) }
        .toBorderPane()

    private val fleetGridPane = fleetGridController
        .activeFleetGrid()
        .apply { addShips(modelView.shipsOf(currentPlayer)) }
        .toBorderPane()



    fun fleetGrid() = fleetGridPane
    fun fleetReadinessPane() = fleetReadinessPane
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