package org.home.mvc.view.battle

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ListView
import org.home.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.PlayerIsReadyAccepted
import org.home.mvc.contoller.events.PlayerTurnToShoot
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.ReadyPlayersAccepted
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
import org.home.utils.functions.ifNotNull
import org.home.utils.functions.or
import org.home.utils.log
import org.home.utils.logTransit
import org.koin.core.component.KoinComponent
import tornadofx.View
import tornadofx.borderpane
import tornadofx.flowpane
import tornadofx.label
import tornadofx.listview
import tornadofx.onChange
import tornadofx.onLeftClick
import tornadofx.selectedItem
import kotlin.collections.set


class BattleView : View("Battle View"), KoinComponent {

    private val model: BattleModel by di()
    private val appProps: ApplicationProperties by di()
    private val battleController: BattleController by di()

    private val selectedPlayer = SimpleStringProperty()
    private val turn = SimpleStringProperty().apply { onChange { listView.refresh() } }
    private val enemyFleetFields = hashMapOf<String, FleetGrid>()
    private lateinit var listView: ListView<String>

    private val fleetGridCreator = FleetGridCreator(
        model.height.value,
        model.width.value
    )

    private val currentPlayer = appProps.currentPlayer
    private val currentView = this@BattleView

    init {
        subscribe<PlayerIsReadyAccepted> {
            listView.refresh()
            currentView.log(it) {
                "${it.player} is ready"
            }
        }

        subscribe<ReadyPlayersAccepted> { event ->
            listView.refresh()
            currentView.log(event, event.players) { "ready: $it" }
        }

        subscribe<PlayerTurnToShoot> {
            turn.value = it.player
            if (currentPlayer == it.player) {
                enemyFleetFields.forEach { (enemy, fleetField) ->
                    fleetField.isDisable = false
                }
            } else {
                enemyFleetFields.forEach { (enemy, fleetField) ->
                    fleetField.isDisable = true
                }
            }
        }

        subscribe<PlayerWasConnected> {
            model.playersAndShips[it.playerName] = mutableListOf()
        }

        model.playersNames.forEach { name ->
            if (name == currentPlayer) return@forEach
            addEnemyFleet(name)
        }
    }

    private fun addEnemyFleet(playerName: String) {
        enemyFleetFields[playerName] = enemyFleetGrid(playerName, model).apply { isDisable = true }
    }

    private val enemiesFleetsPane = borderpane {
        model.playersNames
            .firstOrNull { it != currentPlayer }
            .ifNotNull {
                selectedPlayer.value = it
                center = enemyFleetFields[it]!!
            } or {
                run { center = fleetGridCreator.fleetGrid().addFleetCellClass(AppStyles.enemyCell) }
            }
    }

    override val root = centerGrid {
        row(0) {
            col(0) { label(currentPlayer) { alignment = Pos.BASELINE_LEFT } }
            col(1) {
                flowpane {
                    label("Ходит: ")
                    label(turn) { alignment = Pos.BASELINE_LEFT }
                }
            }

            col(2) { flowpane {
                alignment = Pos.BASELINE_RIGHT
                label(selectedPlayer) }
            }
        }

        row(1) {
            col(0) { currentPlayerFleetGrid(model) }
            col(1) {
                listview<String>(model.playersNames)
                    .apply { listView = this }
                    .apply { viewEnemyFleetOnSelection() }
                    .apply { cellFactory = MarkReadyPlayersCells(model) }
            }
            col(2) { enemiesFleetsPane.also { add(it) } }
        }

        cell(2, 0) {
            logTransit(model, "backTransit", this@BattleView, FleetGridCreationView::class)
            backTransit(this@BattleView, FleetGridCreationView::class)
        }
    }


    private fun ListView<String>.viewEnemyFleetOnSelection() {
        selectionModel.selectedItemProperty().addListener { _, _, newVal ->
            if (newVal == currentPlayer) return@addListener
            selectedPlayer.value = newVal
            enemiesFleetsPane.center = enemyFleetFields[selectedPlayer.value]
        }
    }

    private fun EventTarget.currentPlayerFleetGrid(model: BattleModel) = fleetGrid()
        .addShips(model.playersAndShips[currentPlayer]!!)
        .addFleetCellClass(AppStyles.playerCell)

    private fun enemyFleetGrid(name: String, model: BattleModel) = fleetGrid()
        .addShips(model.playersAndShips[name]!!)
        .addFleetCellClass(AppStyles.enemyCell)
        .onEachFleetCells {
            it.onLeftClick {
                val enemyToHit = listView.selectedItem
                val hitCoord = it.coord
                battleController.hit(enemyToHit!!, hitCoord)
            }
        }

    private fun EventTarget.fleetGrid(): FleetGrid {
        return fleetGridCreator.fleetGrid().also { add(it) }
    }
}
