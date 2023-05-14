package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.exclude
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import org.home.app.AbstractApp.Companion.newGame
import org.home.mvc.AppView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.view.AbstractGameView
import org.home.mvc.view.battle.subscriptions.battleIsEnded
import org.home.mvc.view.battle.subscriptions.battleIsStarted
import org.home.mvc.view.battle.subscriptions.connectedPlayersReceived
import org.home.mvc.view.battle.subscriptions.fleetsReadinessReceived
import org.home.mvc.view.battle.subscriptions.playerIsNotReadyReceived
import org.home.mvc.view.battle.subscriptions.playerIsReadyReceived
import org.home.mvc.view.battle.subscriptions.playerLeaved
import org.home.mvc.view.battle.subscriptions.playerTurnToShoot
import org.home.mvc.view.battle.subscriptions.playerWasConnected
import org.home.mvc.view.battle.subscriptions.playerWasDefeated
import org.home.mvc.view.battle.subscriptions.playerWasDisconnected
import org.home.mvc.view.battle.subscriptions.readyPlayersReceived
import org.home.mvc.view.battle.subscriptions.shipWasAdded
import org.home.mvc.view.battle.subscriptions.shipWasDeleted
import org.home.mvc.view.battle.subscriptions.shipWasHit
import org.home.mvc.view.battle.subscriptions.subscriptions
import org.home.mvc.view.battle.subscriptions.thereWasAMiss
import org.home.mvc.view.components.BattleStartButton
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.GridPaneExtensions.col
import org.home.mvc.view.components.GridPaneExtensions.row
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.components.battleStartButton
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.openAlertWindow
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.battle.subscriptions.serverTransferReceived
import org.home.mvc.view.battle.subscriptions.shipWasSunk
import org.home.style.AppStyles
import org.home.style.StyleUtils.leftPadding
import org.home.style.StyleUtils.rightPadding
import org.home.utils.log
import tornadofx.action
import tornadofx.addClass
import tornadofx.flowpane
import tornadofx.gridpane
import tornadofx.label
import tornadofx.onLeftClick
import tornadofx.selectedItem
import kotlin.collections.set

class BattleView : AbstractGameView("Battle View") {
    internal val battleController: BattleController<Action> by di()

    private val fleetGridController: FleetGridController by newGame()
    private val shipsTypesPaneController: ShipsTypesPaneController by newGame()
    internal val currentPlayerFleetGridPane = BorderPane().apply { center = fleetGridController.activeFleetGrid() }

    internal val currentPlayerFleetReadinessPane = currentPlayerFleetReadinessPane(currentPlayer)
    private fun currentPlayerFleetReadinessPane(player: String) =
        shipsTypesPaneController.shipTypesPane(player).transposed().apply { leftPadding(10) }

    private val selectedEnemyLabel: Label

    private fun enemyFleetReadinessPane(player: String) =
        shipsTypesPaneController.shipTypesPane(player).transposed().flip().apply { rightPadding(10) }

    internal val enemiesFleetGridsPanes = hashMapOf<String, FleetGrid>()

    internal val enemiesFleetsReadinessPanes = hashMapOf<String, ShipsTypesPane>()
    private val selectedEnemyFleetReadinessPane = BorderPane()

    private val emptyFleetGrid = fleetGridController
        .fleetGrid()
        .addFleetCellClass(AppStyles.titleCell)
        .disable()

    internal fun FleetGrid.disable(): FleetGrid {
        isDisable = true
        return this
    }

    internal fun restoreCurrentPlayerFleetGrid() {
        currentPlayerFleetGridPane.center =
            fleetGridController
                .fleetGrid()
                .addShips(model.shipsOf(currentPlayer))
    }

    private val selectedEnemyFleetPane = BorderPane().apply { center = emptyFleetGrid }

    private val playersListView: ListView<String> = ListView(model.players).apply {
        cellFactory = MarkReadyPlayers(model)

        changeEnemyFleetOnSelection()
        id = AppStyles.playersListView

        subscriptions {
            readyPlayersReceived()
        }

        selectedEnemyLabel = label(selectionModel.selectedItemProperty())
    }

    internal lateinit var battleViewExitButton: Button

    internal lateinit var battleStartButton: BattleStartButton

    init {
        title = currentPlayer.uppercase()

        primaryStage.setOnCloseRequest { battleController.onWindowClose() }

        model {

            listOf(turn, defeatedPlayers).forEach {
                it.addListener { _, _, new -> new?.run { playersListView.refresh() } }
            }

            playersReadiness.addValueListener {
                battleStartButton.updateStyle(this@BattleView)
                playersListView.refresh()
            }

            players
                .exclude(currentPlayer)
                .forEach { name ->
                    addEnemyFleetGrid(name)
                    addEnemyFleetReadinessPane(name)
                }
        }

        title = currentPlayer.uppercase()

        selectedEnemyFleetPane { initByFirstIfPresent(enemiesFleetGridsPanes) { disable() } }
        selectedEnemyFleetReadinessPane { initByFirstIfPresent(enemiesFleetsReadinessPanes) }
    }

    private inline fun <N : Node>
            BorderPane.initByFirstIfPresent(
        playersNodes: Map<String, N>,
        afterInit: N.() -> Unit = {},
    ) {
        model {
            players
                .exclude(currentPlayer)
                .firstOrNull()
                ?.also { player ->
                    selectedPlayer.value = player
                    val node = playersNodes[player]!!
                    center = node
                    node.afterInit()
                }
        }
    }

    override val root: GridPane

    override fun exit() {
        battleController.disconnect()
        super.exit()
    }

    val battleViewExitButtonIndices = 2 to 0

    init {
        subscriptions {
            playerWasConnected()
            connectedPlayersReceived()
            fleetsReadinessReceived()
            readyPlayersReceived()
            playerIsReadyReceived()
            playerIsNotReadyReceived()
            shipWasAdded()
            shipWasDeleted()
            battleIsStarted()
            playerTurnToShoot()
            shipWasHit()
            shipWasSunk()
            thereWasAMiss()
            playerLeaved()
            playerWasDefeated()
            playerWasDisconnected()
            battleIsEnded()
            serverTransferReceived()
        }

        root = centerGrid {
            addClass(AppStyles.debugClass)
            row(0) {
                col(0) {
                    flowpane {
                        alignment = Pos.BASELINE_LEFT
                        label(currentPlayer)
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
                col(1) { playersListView.also { add(it) } }
                col(2) {
                    gridpane {
                        cell(0, 0) { selectedEnemyFleetReadinessPane.also { add(it) } }
                        cell(0, 1) { selectedEnemyFleetPane.also { add(it) } }
                    }
                }
            }

            cell(battleViewExitButtonIndices.first, battleViewExitButtonIndices.second) {
                backTransitButton<AppView>(this@BattleView) {
                    battleController.onBattleViewExit()
                }.also {
                    battleViewExitButton = it
                }
            }

            cell(2, 1) {
                battleStartButton(if (applicationProperties.isServer) "В бой" else "Готов") {
                    isDisable = true

                    action {
                       log { "battleController: ${battleController.name}" }
                       battleController.startBattle()
                    }
                }.also {
                    battleStartButton = it
                }
            }
        }
    }


    internal fun addNewFleet(connectedPlayer: String) {
        model.playersAndShips[connectedPlayer] = mutableListOf()
        addSelectedPlayer(connectedPlayer)
        addEnemyFleetGrid(connectedPlayer)
        addEnemyFleetReadinessPane(connectedPlayer)
    }


    private fun addSelectedPlayer(player: String) {
        model.selectedPlayer {
            value ?: run { value = player; return }
            value.ifBlank { value = player }
        }
    }


    private fun addEnemyFleetGrid(enemy: String) {
        enemiesFleetGridsPanes[enemy] =
            enemyFleetGrid().apply {
                isDisable = true
                if (selectedEnemyFleetPane.center == emptyFleetGrid) {
                    selectedEnemyFleetPane.center = this@apply
                }
            }
    }

    private fun addEnemyFleetReadinessPane(enemy: String) {
        enemiesFleetsReadinessPanes[enemy] =
            enemyFleetReadinessPane(enemy).apply {
                selectedEnemyFleetReadinessPane.center ?: run {
                    selectedEnemyFleetReadinessPane.center = this@apply
                }
            }
    }

    private fun ListView<String>.changeEnemyFleetOnSelection() {
        selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new == currentPlayer) return@addListener

            val playerWasRemoved = new == null
            if (playerWasRemoved) {
                items
                    .firstOrNull { it != old && it != currentPlayer }
                    ?.also { setEnemyToPane(it) }
            } else {
                setEnemyToPane(new)
            }
        }
        log { "selected: $selectedItem" }
    }

    private fun setEnemyToPane(name: String) {
        log { "set to panes: $name" }
        model.selectedPlayer.value = name
        selectedEnemyFleetPane.center = enemiesFleetGridsPanes[name]
        selectedEnemyFleetReadinessPane.center = enemiesFleetsReadinessPanes[name]
    }

    internal fun removeEnemyFleet(player: String) {
        enemiesFleetsReadinessPanes.remove(player)
        enemiesFleetGridsPanes.remove(player)
        log { "removed from panes: $player" }

        if (enemiesFleetGridsPanes.size == 0 && enemiesFleetsReadinessPanes.size == 0) {
            selectedEnemyFleetPane.center = emptyFleetGrid
            selectedEnemyFleetReadinessPane.center = null
        }
    }

    private fun EventTarget.currentPlayerFleet() =
        currentPlayerFleetGridPane
            .addClass(AppStyles.currentPlayerCell)
            .also { add(it) }
            .also {
                applicationProperties.ships?.apply {
                    (it.center as FleetGrid).addShips(this)
                }
            }


    private fun enemyFleetGrid() =
        fleetGridController
            .fleetGrid()
            .addFleetCellClass(AppStyles.enemyCell)
            .onEachFleetCells {
                it.onLeftClick {
                    val enemyToHit = playersListView.selectedItem
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

    internal fun Map<String, FleetGrid>.enable() {
        forEach { (_, fleetField) ->
            fleetField.isDisable = false
        }
    }

    internal fun Map<String, FleetGrid>.disable() {
        forEach { (_, fleetField) ->
            fleetField.isDisable = true
        }
    }
}


