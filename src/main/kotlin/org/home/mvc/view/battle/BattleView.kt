package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.model.BattleViewModel
import org.home.mvc.view.battle.subscription.SubscriptionComponent
import org.home.mvc.view.battle.subscription.readyPlayersReceived
import org.home.mvc.view.battle.subscription.subscribe
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.button.BattleStartButtonController
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.style.FleetGridStyleComponent
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CLASS
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TRANSITION
import org.home.style.AppStyles
import tornadofx.addClass
import tornadofx.flowpane
import tornadofx.gridpane
import tornadofx.label

class BattleView : GameView("Battle View") {
    internal val readinessStyleComponent by gameScope<FleetGridStyleComponent>(CLASS)
    internal val shotStyleComponent by gameScope<FleetGridStyleComponent>(TRANSITION)
    internal val defeatedStyleComponent by gameScope<FleetGridStyleComponent>(TRANSITION)

    internal val battleController by noScope<BattleController<Action>>()

    internal val battleStartButtonController by gameScope<BattleStartButtonController>()

    internal val currentFleetController by gameScope<CurrentFleetController>()
    internal val enemiesView by gameScope<EnemiesViewController>()
    internal val subscriptionComponent by gameScope<SubscriptionComponent>()

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
    internal var battleStartButton = battleStartButtonController.create()

    init {
        title = currentPlayer.uppercase()

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

    override fun onClose() {
        battleController.leaveBattle()
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

        viewSwitchButtonController {
            cell(row, indices.second) {
                leaveButton(currentView()).also { battleViewExitButton = it }
            }
        }

        cell(row, 1) {
            battleStartButton.also { add(it) }
        }
    }
}
