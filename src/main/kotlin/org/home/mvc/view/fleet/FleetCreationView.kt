package org.home.mvc.view.fleet

import javafx.event.EventTarget
import org.home.app.ApplicationProperties
import org.home.app.injecting
import org.home.mvc.contoller.PlayersNamesPaneController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.row
import org.home.mvc.view.components.transit
import org.home.style.AppStyles
import org.koin.core.component.KoinComponent
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button

class FleetCreationView : View("Создание флота"), KoinComponent {

    private val model: BattleModel by injecting()
    private val applicationProperties: ApplicationProperties by injecting()

    private val currentPlayer = applicationProperties.currentPlayer

    private val fleetCreationBox: FleetCreationBox = FleetCreationBox(currentPlayer, model)

    private val shipsTypesPaneController: ShipsTypesPaneController by injecting()
    private val playersNamesPaneController: PlayersNamesPaneController by injecting()

    override val root =
        centerGrid {

            row(0) {
                col(0) { playersNamesPaneController.playersListView.also { add(it) } }
                col(1) { fleetCreationBox.also { add(it) } }
                col(2) { shipsTypesInfoPane() }
            }

            row(2) {
                col(0) { backTransit(this@FleetCreationView, BattleCreationView::class) }
                col(1) { clearFieldButton() }
                col(2) { transit(this@FleetCreationView, BattleView::class, "В бой") }
            }
        }

    private fun EventTarget.shipsTypesInfoPane() =
        shipsTypesPaneController.shipTypesPane().also { add(it); it.addClass(AppStyles.shipsTypesInfoPane) }

    private fun EventTarget.clearFieldButton() = button("Очистить") {
        action {
            model.playersAndShips[currentPlayer]!!.clear()
            TODO("FleetBoxController is needed that will handle an event of fleet grid clearance")
        }
    }
}

