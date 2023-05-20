package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import org.home.app.di.GameScope
import org.home.mvc.AppView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.model.BattleViewModel
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
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass
import org.home.mvc.view.fleet.style.FleetGridStyleTransition
import org.home.style.AppStyles
import tornadofx.addClass
import tornadofx.flowpane
import tornadofx.gridpane
import tornadofx.label

class BattleView : GameView("Battle View") {
    internal val readinessStyleComponent by GameScope.fleetGridStyle<FleetGridStyleAddClass>()
    internal val shotStyleComponent by GameScope.fleetGridStyle<FleetGridStyleTransition>()
    internal val defeatedStyleComponent by GameScope.fleetGridStyle<FleetGridStyleTransition>()

    internal val battleController by di<BattleController<Action>>()

    internal val startButtonController by GameScope.inject<BattleStartButtonController>()

    internal val currentFleetController by GameScope.inject<CurrentFleetController>()
    internal val enemiesView by GameScope.inject<EnemiesViewController>()

    internal val currentFleetGridPane = currentFleetController.fleetGrid()
    internal val currentFleetReadinessPane = currentFleetController.fleetReadinessPane()

    internal val enemiesFleetGridsPanes      = enemiesView.fleetGridsPanes
    internal val enemiesFleetsReadinessPanes = enemiesView.fleetsReadinessPanes

    private val selectedLabel                = enemiesView.selectedEnemyLabel
    private val selectedFleetPane            = enemiesView.selectedFleetPane
    private val selectedFleetReadinessPane   = enemiesView.selectedFleetReadinessPane

    fun fleets(): Map<String, FleetGrid> {
        return enemiesFleetGridsPanes + (currentPlayer to currentFleetGridPane.center as FleetGrid)
    }

    fun fleetsReadiness(): Map<String, ShipsTypesPane> {
        return enemiesFleetsReadinessPanes + (currentPlayer to currentFleetReadinessPane.center as ShipsTypesPane)
    }

    fun fleets(player: String): FleetGrid {
        return modelView
            .hasCurrent(player)
            .then { currentFleetGridPane.center as FleetGrid }
            .or { enemiesFleetGridsPanes[player]!! }
    }

    fun fleetsReadiness(player: String? = null): ShipsTypesPane {
        return modelView
            .hasCurrent(player)
            .then { currentFleetReadinessPane.center as ShipsTypesPane }
            .or { enemiesFleetsReadinessPanes[player]!! }
    }



    private val currentPlayerLabel = currentFleetController.playerLabel()

    fun BattleViewModel.playerLabel(player: String) = player.isCurrent then currentPlayerLabel or selectedLabel

    internal lateinit var battleViewExitButton: Button
    internal var battleStartButton = startButtonController.create()

    init {
        title = currentPlayer.uppercase()

        primaryStage.setOnCloseRequest { battleController.onWindowClose() }

        modelView {
            listOf(
                turn,
                getDefeatedPlayers(),
                getReadyPlayers()
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
                        cell(0, 0) { currentFleetGridPane.also { add(it) } }
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
}
