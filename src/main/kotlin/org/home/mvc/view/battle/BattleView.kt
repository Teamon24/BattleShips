package org.home.mvc.view.battle

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.model.BattleModel
import org.home.mvc.model.notAllReady
import org.home.mvc.view.app.AppView
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
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.GridPaneExtensions.col
import org.home.mvc.view.components.GridPaneExtensions.row
import org.home.mvc.view.components.backTransferButton
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.openAlertWindow
import org.home.style.AppStyles
import org.home.style.StyleUtils.leftPadding
import org.home.style.StyleUtils.rightPadding
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.log
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.flowpane
import tornadofx.gridpane
import tornadofx.label
import tornadofx.listview
import tornadofx.onLeftClick
import tornadofx.removeClass
import tornadofx.selectedItem
import kotlin.collections.set


class BattleView : View("Battle View") {
    internal val model: BattleModel by di()
    internal val battleController: BattleController by di()
    internal val appProps: ApplicationProperties by di()
    internal val currentPlayer = appProps.currentPlayer

    init {
        model.playersAndShips[appProps.currentPlayer] = mutableListOf()
    }

    internal val fleetGridController: FleetGridController by di()
    private val shipsTypesPaneController: ShipsTypesPaneController by di()

    internal val currentPlayerFleetGridPane = BorderPane().apply { center = fleetGridController.activeFleetGrid() }
    internal val currentPlayerFleetReadiness = currentPlayerFleetReadinessPane(currentPlayer)

    private fun currentPlayerFleetReadinessPane(player: String) =
        shipsTypesPaneController.shipTypesPane(player).transpose().apply { leftPadding(10) }

    private fun enemyFleetReadinessPane(player: String) =
        shipsTypesPaneController.shipTypesPane(player).transpose().flip().apply { rightPadding(10) }

    internal val enemiesFleetsFleetGrids = hashMapOf<String, FleetGrid>()
    internal val enemiesFleetsReadinessPanes = hashMapOf<String, ShipsTypesPane>()

    private val selectedEnemyFleetReadinessPane = BorderPane()

    private val emptyFleetGrid = createEmptyFleetGreed()
    private val selectedEnemyFleetPane = BorderPane().apply { center = emptyFleetGrid }

    internal lateinit var playersListView: ListView<String>
    internal lateinit var battleViewExitButton: Button
    internal lateinit var battleButton: Button

    private fun createEmptyFleetGreed() =
        fleetGridController
            .fleetGrid()
            .addFleetCellClass(AppStyles.titleCell)
            .disable()

    init {
        title = currentPlayer.uppercase()

        primaryStage.setOnCloseRequest {
            battleController.onWindowClose()
        }

        model.playersReadiness.addValueListener {
            battleButton.updateStyle()
            playersListView.refresh()
        }

        model.turn.addListener { _, _, _ ->
            playersListView.refresh()
        }

        model.defeatedPlayers.addListener { _, _, new ->
            new?.run {
                playersListView.refresh()
            }
        }

        primaryStage.setOnCloseRequest {
            battleController.onWindowClose()
        }

        title = currentPlayer.uppercase()

        model.log { "players = $playersNames" }

        model.playersNames
            .exclude(currentPlayer)
            .forEach { name ->
                addEnemyFleetGrid(name)
                addEnemyFleetReadinessPane(name)
            }

        selectedEnemyFleetPane {
            initByFirstIfPresent(enemiesFleetsFleetGrids) { disable() }
        }

        selectedEnemyFleetReadinessPane {
            initByFirstIfPresent(enemiesFleetsReadinessPanes)
        }
    }

    private inline
    fun <N : Node>
            BorderPane.initByFirstIfPresent(
                playersNodes: Map<String, N>,
                afterInit: N.() -> Unit = {}
    ) {
        model.playersNames
            .exclude(currentPlayer)
            .firstOrNull()
            ?.also { player ->
                model.selectedPlayer.value = player
                val node = playersNodes[player]!!
                center = node
                node.afterInit()
            }
    }

    override val root: GridPane

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
            thereWasAMiss()
            playerLeaved()
            playerWasDefeated()
            playerWasDisconnected()
            battleIsEnded()
        }

        root = centerGrid root@{
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
                        label(model.selectedPlayer)
                    }
                }
            }

            row(1) {
                col(0) {
                    gridpane {
                        cell(0, 0) { currentPlayerFleet() }
                        cell(0, 1) { currentPlayerFleetReadiness() }
                    }
                }
                col(1) { playersListview() }
                col(2) {
                    gridpane {
                        cell(0, 0) { selectedEnemyFleetReadiness() }
                        cell(0, 1) { selectedEnemyFleet() }
                    }
                }
            }

            cell(2, 0) {
                backTransitButton(this@BattleView, AppView::class) {
                    battleController.onBattleViewExit()
                }.also {
                    battleViewExitButton = it
                }
            }

            cell(2, 1) {
                button(if (appProps.isServer) "В бой" else "Готов") {
                    if (!appProps.isServer) isDisable = true
                    updateStyle()
                    action {
                       log { "battleController: ${battleController.name}" }
                       battleController.startBattle()
                    }
                }.also {
                    battleButton = it
                }
            }
        }
    }

    private fun EventTarget.playersListview(): ListView<String> {
        return listview<String>(model.playersNames) {
            playersListView = this
            cellFactory = MarkReadyPlayers(model)
            changeEnemyFleetOnSelection()
            id = AppStyles.playersListView
            subscriptions {
                readyPlayersReceived()
            }
        }
    }

    private fun EventTarget.currentPlayerFleetReadiness(): ShipsTypesPane {
        return currentPlayerFleetReadiness.also { add(it) }
    }

    private fun EventTarget.selectedEnemyFleetReadiness(): BorderPane {
        return selectedEnemyFleetReadinessPane.also { add(it) }
    }

    private fun EventTarget.selectedEnemyFleet(): BorderPane {
        return selectedEnemyFleetPane.also { add(it) }
    }

    internal fun Button.updateStyle() {
        if (appProps.isServer) {
            isDisable = model.notAllReady
                .yes { removeClass(AppStyles.readyButton) }
                .no { addClass(AppStyles.readyButton) }
        } else {
            when(model.hasReady(currentPlayer)) {
                true -> addClass(AppStyles.readyButton)
                else -> removeClass(AppStyles.readyButton)
            }
        }
    }

    internal fun addEnemyFleetGrid(enemy: String) {
        enemiesFleetsFleetGrids[enemy] =
            enemyFleetGrid().apply {
                isDisable = true
                if (selectedEnemyFleetPane.center == emptyFleetGrid) {
                    selectedEnemyFleetPane.center = this@apply
                }
            }
    }

    internal fun addEnemyFleetReadinessPane(enemy: String) {
        enemiesFleetsReadinessPanes[enemy] =
            enemyFleetReadinessPane(enemy).apply {
                selectedEnemyFleetReadinessPane.center ?: run {
                    selectedEnemyFleetReadinessPane.center = this@apply
                }
            }
    }

    private fun ListView<String>.changeEnemyFleetOnSelection() {
        selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (new == currentPlayer) {
                return@addListener
            }

            val playerWasRemoved = new == null
            if (playerWasRemoved) {
                log { "selectedItem == old" }
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
        selectedEnemyFleetPane.center = enemiesFleetsFleetGrids[name]
        selectedEnemyFleetReadinessPane.center = enemiesFleetsReadinessPanes[name]
    }

    internal fun removeEnemy(player: String) {
        enemiesFleetsReadinessPanes.remove(player)
        enemiesFleetsFleetGrids.remove(player)
        log { "removed from panes: $player" }

        if (enemiesFleetsFleetGrids.size == 0 && enemiesFleetsReadinessPanes.size == 0) {
            selectedEnemyFleetPane.center = emptyFleetGrid
            selectedEnemyFleetReadinessPane.center = null
        }
    }

    private fun EventTarget.currentPlayerFleet() =
        currentPlayerFleetGridPane.addClass(AppStyles.currentPlayerCell).also { add(it) }


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

    fun FleetGrid.disable(): FleetGrid {
        isDisable = true
        return this
    }

    fun addSelectedPlayer(player: String) {
        model.selectedPlayer {
            value ?: run { value = player; return }
            value.ifBlank { value = player }
        }
    }

    fun restoreCurrentPlayerFleetGrid() {
        currentPlayerFleetGridPane.center =
            fleetGridController
                .fleetGrid()
                .addShips(model.playersAndShips[currentPlayer]!!)
    }
}


