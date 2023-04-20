package org.home.mvc.view.fleet

import javafx.event.EventTarget
import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.events.PlayerIsReadyAccepted
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.PlayersAccepted
import org.home.mvc.contoller.events.ReadyPlayersAccepted
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.MarkReadyPlayersCells
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.row
import org.home.mvc.view.components.transit
import org.home.style.AppStyles
import org.home.utils.log
import org.home.utils.logEach
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.listview

class FleetGridCreationView : View("Создание флота") {

    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()
    private val currentPlayer = applicationProperties.currentPlayer

    private val fleetGridCreationComponent: FleetGridCreationComponent by di()
    private val shipsTypesPaneController: ShipsTypesPaneController by di()
    private val battleController: BattleController by di()

    private val listView = listview(model.playersNames) {
        cellFactory = MarkReadyPlayersCells(model)
    }

    override val root = centerGrid()
    private val currentView = this@FleetGridCreationView

    init {
        this.title = applicationProperties.currentPlayer.uppercase()

        subscribe<PlayerWasConnected> {
            model.playersAndShips[it.playerName] = mutableListOf()
        }

        subscribe<PlayerIsReadyAccepted> {
            listView.refresh()
            currentView.log(it) { "${it.player} is ready" }
        }

        subscribe<ReadyPlayersAccepted> { event ->
            listView.refresh()
            currentView.logEach(event, event.players) { "ready: $it" }
        }

        subscribe<PlayersAccepted> {
            it.players.forEach { player ->
                model.playersAndShips[player] = mutableListOf()
            }
        }

        with(root) {
            row(0) {
                col(0) { listView.also { add(it) } }
                col(1) { fleetGridCreationComponent.root.also { add(it) } }
                col(2) { shipsTypesInfoPane() }
            }

            row(2) {
                col(0) { backTransit(currentView, BattleCreationView::class) }
                col(1) { clearFieldButton() }
                col(2) {
                    transit(currentView, BattleView::class, "В бой") {
                        battleController.startBattle()
                    }
                }
            }
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

