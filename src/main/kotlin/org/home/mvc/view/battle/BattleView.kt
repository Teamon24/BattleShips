package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import org.home.app.di.GameScope
import org.home.mvc.AppView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.model.BattleModel
import org.home.mvc.view.AbstractGameView
import org.home.mvc.view.battle.subscription.readyPlayersReceived
import org.home.mvc.view.battle.subscription.subscribe
import org.home.mvc.view.component.button.BattleStartButtonController
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.backTransitButton
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass
import org.home.mvc.view.fleet.style.FleetGridStyleTransition
import org.home.mvc.view.openAlertWindow
import org.home.style.AppStyles
import org.home.style.StyleUtils.leftPadding
import org.home.utils.log
import tornadofx.addClass
import tornadofx.flowpane
import tornadofx.gridpane
import tornadofx.label
import tornadofx.onLeftClick
import tornadofx.removeClass
import tornadofx.selectedItem
import kotlin.collections.set

class BattleView : AbstractGameView("Battle View") {
    internal val readinessStyleComponent by GameScope.fleetGridStyle<FleetGridStyleAddClass>()
    internal val defeatedStyleComponent by GameScope.fleetGridStyle<FleetGridStyleTransition>()

    internal val battleController by di<BattleController<Action>>()

    private val fleetGridController by GameScope.inject<FleetGridController>()
    internal val shipsTypesPaneController by GameScope.inject<ShipsTypesPaneController>()
    internal val startButtonController by GameScope.inject<BattleStartButtonController>()

    internal val playersFleetGridsPanes = hashMapOf<String, FleetGrid>()
    internal val playersFleetsReadinessPanes = hashMapOf<String, ShipsTypesPane>()

    private val currentPlayerFleetGridPane = BorderPane(fleetGridController.activeFleetGrid()).apply {
        playersFleetGridsPanes[currentPlayer] = center as FleetGrid
    }

    private val currentPlayerFleetReadinessPane =
        shipsTypesPaneController
            .shipTypesPane(currentPlayer)
            .transposed().apply {
                leftPadding(10)
                playersFleetsReadinessPanes[currentPlayer] = this
            }

    internal val selectedEnemyFleetPane = BorderPane()
    internal val selectedEnemyFleetReadinessPane = BorderPane()

    internal val enemiesView =
        EnemiesView(model.enemies, model) { selected ->
            log { "set to panes: $selected" }
            selectedEnemyLabel.text = selected
            selectedEnemyFleetPane.center = playersFleetGridsPanes[selected]!!
            selectedEnemyFleetReadinessPane.center = playersFleetsReadinessPanes[selected]
        }.apply {
            this.refresh()
        }

    internal val selectedEnemyLabel: Label = enemiesView.getSelectedEnemyLabel()
    private val currentPlayerLabel = Label(currentPlayer).addClass(AppStyles.currentPlayerLabel)

    fun BattleModel.playerLabel(player: String) = player.isCurrent then currentPlayerLabel or selectedEnemyLabel

    internal lateinit var battleViewExitButton: Button

    internal var battleStartButton = startButtonController.create()

    init {
        title = currentPlayer.uppercase()

        primaryStage.setOnCloseRequest { battleController.onWindowClose() }

        model {
            listOf(
                turn,
                defeatedPlayers,
                readyPlayers
            ).forEach {
                it.addListener { _, _, new -> new?.run { enemiesView.refresh() } }
            }
        }

        title = currentPlayer.uppercase()

        selectedEnemyFleetPane { initByFirstIfPresent(playersFleetGridsPanes) { disable() } }
        selectedEnemyFleetReadinessPane.initByFirstIfPresent(playersFleetsReadinessPanes)
    }

    private inline fun <N : Node> BorderPane.initByFirstIfPresent(
        playersNodes: Map<String, N>,
        afterInit: N.() -> Unit = {},
    ) {
        model {
            enemies.firstOrNull()?.also { player -> center = playersNodes[player]!!.apply { afterInit() } }
        }
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
                        label(model.turn) { alignment = Pos.BASELINE_CENTER }
                    }
                }

                col(2) {
                    flowpane {
                        alignment = Pos.BASELINE_RIGHT
                        add(selectedEnemyLabel)
                    }
                }
            }

            row(1) {
                col(0) {
                    gridpane {
                        cell(0, 0) { currentPlayerFleet() }
                        cell(0, 1) { currentPlayerFleetReadinessPane.also { add(it) } }
                    }
                }
                col(1) {
                    readyPlayersReceived()
                    enemiesView.also { add(it) }
                }
                col(2) {
                    gridpane {
                        cell(0, 0) { selectedEnemyFleetReadinessPane.also { add(it) } }
                        cell(0, 1) { selectedEnemyFleetPane.also { add(it) } }
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
        currentPlayerFleetGridPane
            .addClass(AppStyles.currentPlayerCell)
            .also { add(it) }
            .also { pane ->
                model.shipsOf(currentPlayer).let {
                    (pane.center as FleetGrid).addShips(it)
                }
            }

    internal fun enemyFleetGrid() =
        fleetGridController
            .fleetGrid()
            .addFleetCellClass(AppStyles.enemyCell)
            .onEachFleetCells {
                it.onLeftClick {
                    val enemyToHit = enemiesView.selectedItem
                    if (enemyToHit == null) {
                        openAlertWindow { "Выберите игрока для выстрела" }
                        return@onLeftClick
                    }

                    val hitCoord = it.coord
                    log { "shooting $hitCoord" }
                    model.hasNo(enemyToHit, hitCoord).so {
                        battleController.shot(enemyToHit, hitCoord)
                    }
                }
            }

    internal fun updateCurrentPlayerFleetGrid() {
        fleetGridController
            .fleetGrid()
            .addShips(model.shipsOf(currentPlayer))
            .onEachFleetCells {
                it.removeClass(AppStyles.emptyCell)
            }
            .also {
                playersFleetGridsPanes[currentPlayer] = it
                currentPlayerFleetGridPane.center = it
            }

    }
    internal fun <T: Node> Map<String, T>.disable() { values.forEach { it.disable() } }
    internal fun <T: Node> T.disable() = apply { isDisable = true }

    internal fun BorderPane.disable() = apply { center.isDisable = true }
    internal fun <T: Node> Map<String, T>.enable() { values.forEach { it.enable() } }
    internal fun <T: Node> T.enable() = apply { isDisable = false }

    internal fun BorderPane.enable() = apply { center.isDisable = false }
    internal fun FleetGrid.disableIf(condition: Boolean) = apply { condition.so { isDisable = true } }
}
