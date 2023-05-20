package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import org.home.app.di.GameScope
import org.home.mvc.AppView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.model.BattleModel
import org.home.mvc.view.GameView
import org.home.mvc.view.battle.subscription.readyPlayersReceived
import org.home.mvc.view.battle.subscription.subscribe
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.backTransitButton
import org.home.mvc.view.component.button.BattleStartButtonController
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass
import org.home.mvc.view.fleet.style.FleetGridStyleTimeline
import org.home.mvc.view.fleet.style.FleetGridStyleTransition
import org.home.style.AppStyles
import org.home.utils.StyleUtils.leftPadding
import tornadofx.addClass
import tornadofx.flowpane
import tornadofx.gridpane
import tornadofx.label
import kotlin.collections.set

class BattleView : GameView("Battle View") {
    internal val readinessStyleComponent by GameScope.fleetGridStyle<FleetGridStyleAddClass>()
    internal val shotStyleComponent by GameScope.fleetGridStyle<FleetGridStyleTransition>()
    internal val defeatedStyleComponent by GameScope.fleetGridStyle<FleetGridStyleTransition>()

    internal val battleController by di<BattleController<Action>>()

    private val fleetGridController by GameScope.inject<FleetGridController>()
    private val shipsTypesPaneController by GameScope.inject<ShipsTypesPaneController>()

    internal val startButtonController by GameScope.inject<BattleStartButtonController>()

    internal val enemiesView by GameScope.inject<EnemiesViewController>()

    internal val fleetGridsPanes            = enemiesView.fleetGridsPanes
    internal val fleetsReadinessPanes       = enemiesView.fleetsReadinessPanes
    private val selectedLabel              = enemiesView.selectedEnemyLabel
    private val selectedFleetPane          = enemiesView.selectedFleetPane
    private val selectedFleetReadinessPane = enemiesView.selectedFleetReadinessPane


    private val currentFleetGridPane = BorderPane(fleetGridController.activeFleetGrid()).apply {
        fleetGridsPanes[currentPlayer] = center as FleetGrid
    }

    private val currentFleetReadinessPane = shipsTypesPaneController
        .shipTypesPane(currentPlayer)
        .transposed().apply {
            leftPadding(10)
            fleetsReadinessPanes[currentPlayer] = this
        }

    private val currentPlayerLabel = Label(currentPlayer).addClass(AppStyles.currentPlayerLabel)

    fun BattleModel.playerLabel(player: String) = player.isCurrent then currentPlayerLabel or selectedLabel

    internal lateinit var battleViewExitButton: Button
    internal var battleStartButton = startButtonController.create()

    init {
        title = currentPlayer.uppercase()

        primaryStage.setOnCloseRequest { battleController.onWindowClose() }

        modelView {
            listOf(
                turn,
                defeatedPlayers,
                readyPlayers
            ).forEach {
                it.addListener { _, _, new -> new?.run { enemiesView.refresh() } }
            }
        }

        title = currentPlayer.uppercase()
    }


    override val root: GridPane

    override fun exit() {
        battleController.disconnect()
        super.exit()
    }

    private val battleViewExitButtonRow = 2

    val battleViewExitButtonIndices = battleViewExitButtonRow to 0

    init {
        subscribe()

        root = centerGrid {
            initRow(battleViewExitButtonIndices)

            addClass(AppStyles.debugClass)
            row(0) {
                col(0) {
                    flowpane {
                        alignment = Pos.BASELINE_LEFT
                        add(currentPlayerLabel)
                    }
                }
                col(1) {
                    flowpane {
                        label("Ходит: ")
                        label(modelView.turn) { alignment = Pos.BASELINE_CENTER }
                    }
                }

                col(2) {
                    flowpane {
                        alignment = Pos.BASELINE_RIGHT
                        add(selectedLabel)
                    }
                }
            }

            row(1) {
                col(0) {
                    gridpane {
                        cell(0, 0) { currentPlayerFleet() }
                        cell(0, 1) { currentFleetReadinessPane.also { add(it) } }
                    }
                }
                col(1) {
                    readyPlayersReceived()
                    enemiesView.enemiesList.also { add(it) }
                }
                col(2) {
                    gridpane {
                        cell(0, 0) { selectedFleetReadinessPane.also { add(it) } }
                        cell(0, 1) { selectedFleetPane.also { add(it) } }
                    }
                }
            }
        }
    }

    private fun GridPane.initRow(indices: Pair<Int, Int>) {
        val row = indices.first
        cell(row, indices.second) {
            backTransitButton<AppView>(this@BattleView) {
                battleController.onBattleViewExit()
            }.also {
                battleViewExitButton = it
            }
        }

        cell(row, 1) {
            battleStartButton.also { add(it) }
        }
    }


    private fun EventTarget.currentPlayerFleet() =
        currentFleetGridPane
            .also { add(it) }
            .also { pane ->
                modelView.shipsOf(currentPlayer).let {
                    (pane.center as FleetGrid).addShips(it)
                }
            }



    internal fun updateCurrentPlayerFleetGrid() {
        fleetGridController
            .fleetGrid()
            .addShips(modelView.shipsOf(currentPlayer))
            .also {

                fleetGridsPanes[currentPlayer] = it
                currentFleetGridPane.center = it
            }

    }
}
