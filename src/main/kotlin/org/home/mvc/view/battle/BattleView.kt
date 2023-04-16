package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import org.home.app.ApplicationProperties
import org.home.app.injecting
import org.home.mvc.contoller.BattleController
import org.home.mvc.model.BattleModel
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.col
import org.home.mvc.view.components.row
import org.home.mvc.view.fleet.FleetBoxCreator
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetCreationView
import org.home.style.AppStyles
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import tornadofx.View
import tornadofx.flowpane
import tornadofx.label
import tornadofx.listview
import tornadofx.pane


class BattleView : View("Battle View"), KoinComponent {

    private val model: BattleModel by injecting()
    private val appProps: ApplicationProperties by injecting()
    private val battleController = (this as KoinComponent).get<BattleController> { parametersOf(appProps[ApplicationProperties.isServer]) }

    private val selectedPlayer = SimpleStringProperty()
    private val turn = SimpleStringProperty().apply { value = "Temp" }
    private val fleets = hashMapOf<String, FleetGrid>()

    private val fleetBoxCreator = FleetBoxCreator(
        model.fleetGridHeight.value,
        model.fleetGridWidth.value
    )

    private val currentPlayer = appProps.currentPlayer

    init {
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)
        println(battleController.javaClass.simpleName)

        model.playersNames.forEach { name ->
            if (name == currentPlayer) return@forEach
            fleets[name] = enemyFleetGrid(name)
        }
    }

    private val enemiesFleetsPane = pane {
        val player = model.playersNames.first { it != currentPlayer }!!
        selectedPlayer.value = player
        fleets[player]!!.also { add(it) }
    }

    override val root = centerGrid {
        row(0) {
            col(0) { label(currentPlayer) { alignment = Pos.BASELINE_LEFT } }
            col(1) { flowpane { label("Ходит: "); label(turn) { alignment = Pos.BASELINE_LEFT } } }
            col(2) { label(selectedPlayer) { alignment = Pos.BASELINE_RIGHT } }
        }

        row(1) {
            col(0) { currentPlayerFleetGrid() }
            col(2) { enemiesFleetsPane.also { add(it) } }

            col(1) {
                listview<String>(model.playersNames) {
                    selectionModel.selectedItemProperty().addListener { changed, oldVal, newVal ->
                        if (newVal == currentPlayer) return@addListener
                        selectedPlayer.value = newVal
                        enemiesFleetsPane.children.clear()
                        enemiesFleetsPane.children.add(fleets[selectedPlayer.value])
                        refresh()
                    }
                }
            }
        }

        cell(2, 0) { backTransit(this@BattleView, FleetCreationView::class) }

    }

    private fun EventTarget.currentPlayerFleetGrid() = fleetGrid()
        .addShips(model.playersAndShips[currentPlayer]!!)
        .addFleetCellClass(AppStyles.playerCell)

    private fun enemyFleetGrid(name: String?) = fleetGrid()
        .addShips(model.playersAndShips[name]!!)
        .addFleetCellClass(AppStyles.enemyCell)

    private fun EventTarget.fleetGrid(): FleetGrid {
        return fleetBoxCreator.fleetGrid().also { add(it) }
    }
}
