package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
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
import org.home.mvc.view.component.button.BattleViewExitButtonController
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
    internal val battleController               by noScope<BattleController<Action>>()
    internal val readinessStyleComponent        by gameScope<FleetGridStyleComponent>(CLASS)
    internal val shotStyleComponent             by gameScope<FleetGridStyleComponent>(TRANSITION)
    internal val defeatedStyleComponent         by gameScope<FleetGridStyleComponent>(TRANSITION)
    internal val battleStartButtonController    by gameScope<BattleStartButtonController>()
    internal val battleViewExitButtonController by gameScope<BattleViewExitButtonController>()
    internal val currentFleetController         by gameScope<CurrentFleetController>()
    internal val enemiesViewController          by gameScope<EnemiesViewController>()
    internal val subscriptionComponent          by gameScope<SubscriptionComponent>()

    private lateinit var currentFleetGridPane: BorderPane
    private lateinit var currentFleetReadinessPane: BorderPane

    internal val enemiesFleetGridsPanes      = enemiesViewController.fleetGridsPanes
    internal val enemiesFleetsReadinessPanes = enemiesViewController.fleetsReadinessPanes

    private val selectedLabel                = enemiesViewController.selectedEnemyLabel
    private val selectedFleetPane            = enemiesViewController.selectedFleetPane
    private val selectedFleetReadinessPane   = enemiesViewController.selectedFleetReadinessPane

    override val root: GridPane

    private val currentPlayerLabel = currentFleetController.playerLabel()

    init {
        title = currentPlayer.uppercase()

        modelView {
            listOf(
                turn,
                getDefeatedPlayers(),
                getReadyPlayers()
            ).forEach {
                it.addListener { _, _, new -> new?.run { enemiesViewController.refresh() } }
            }
        }

        title = currentPlayer.uppercase()

        subscribe()

        root = centerGrid {
            battleViewExitButtonController {
                cell(row, col) { create(currentView()) }
                battleStartButtonController { cell(row, 1) { create() } }
            }

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
                        currentFleetController {
                            cell(0, 0) { fleetGridPane().also { currentFleetGridPane = it } }
                            cell(0, 1) { fleetReadinessPane().also { currentFleetReadinessPane = it } }
                        }
                    }
                }
                col(1) {
                    readyPlayersReceived()
                    enemiesViewController.enemiesList.also { add(it) }
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

    fun BattleViewModel.playerLabel(player: String) =
        player
            .isCurrent
            .then(currentPlayerLabel)
            .or { hasDefeated(selectedLabel.text).then(selectedLabel) }

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

    override fun onClose() {
        battleController.leaveBattle()
    }
}
