package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.model.BattleModel
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.row
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridCreationView
import org.home.mvc.view.fleet.FleetGridCreator
import org.home.style.AppStyles
import org.home.utils.logInject
import org.home.utils.logTransit
import org.koin.core.component.KoinComponent
import tornadofx.View
import tornadofx.borderpane
import tornadofx.flowpane
import tornadofx.label
import tornadofx.listview
import kotlin.collections.set


class BattleView : View("Battle View"), KoinComponent {

    private val model: BattleModel by di()
    private val appProps: ApplicationProperties by di()
    private val battleController: BattleController by di()

    private val selectedPlayer = SimpleStringProperty()
    private val turn = SimpleStringProperty().apply { value = "Temp" }
    private val fleets = hashMapOf<String, FleetGrid>()

    private val fleetGridCreator = FleetGridCreator(
        model.height.value,
        model.width.value
    )

    private val currentPlayer = appProps.currentPlayer

    init {
        subscribe<PlayerWasConnected> {
            model.playersAndShips[it.playerName] = mutableListOf()
            org.home.mvc.view.openWindow {
                "${it.playerName} присоединился к игре"
            }
        }

        model.playersNames.forEach { name ->
            if (name == currentPlayer) return@forEach
            fleets[name] = enemyFleetGrid(name)
        }
    }

    private val enemiesFleetsPane = borderpane {
        val player = model.playersNames.first { it != currentPlayer }!!
        selectedPlayer.value = player
        center = fleets[player]!!
    }

    override val root = centerGrid {
        logInject(battleController)
        row(0) {
            col(0) { label(currentPlayer) { alignment = Pos.BASELINE_LEFT } }
            col(1) {
                flowpane { label("Ходит: ");
                label(turn) { alignment = Pos.BASELINE_LEFT } }
            }
            col(2) { flowpane {
                alignment = Pos.BASELINE_RIGHT
                label(selectedPlayer) }
            }
        }

        row(1) {
            col(0) { currentPlayerFleetGrid() }
            col(1) {
                listview<String>(model.playersNames) {
                    selectionModel.selectedItemProperty().addListener { _, _, newVal ->
                        setSelectedEnemyFleet(newVal)
                    }
                }
            }
            col(2) { enemiesFleetsPane.also { add(it) } }
        }

        cell(2, 0) {
            logTransit(model, "backTransit", this@BattleView, FleetGridCreationView::class)
            backTransit(this@BattleView, FleetGridCreationView::class)
        }
    }

    private fun setSelectedEnemyFleet(newVal: String) {
        if (newVal == currentPlayer) return
        selectedPlayer.value = newVal
        enemiesFleetsPane.center = fleets[selectedPlayer.value]
    }

    private fun EventTarget.currentPlayerFleetGrid() = fleetGrid()
        .addShips(model.playersAndShips[currentPlayer]!!)
        .addFleetCellClass(AppStyles.playerCell)

    private fun enemyFleetGrid(name: String?) = fleetGrid()
        .addShips(model.playersAndShips[name]!!)
        .addFleetCellClass(AppStyles.enemyCell)

    private fun EventTarget.fleetGrid(): FleetGrid {
        return fleetGridCreator.fleetGrid().also { add(it) }
    }
}
