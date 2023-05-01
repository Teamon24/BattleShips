package org.home.mvc.view.fleet

import javafx.event.EventTarget
import javafx.scene.control.ListView
import javafx.scene.layout.GridPane
import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.model.BattleModel
import org.home.mvc.model.BattleModel.Companion.fleetReadiness
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.MarkReadyPlayers
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.GridPaneExtensions.col
import org.home.mvc.view.components.GridPaneExtensions.row
import org.home.mvc.view.components.backTransitButton
import org.home.mvc.view.components.transitButton
import org.home.mvc.view.connectedPlayersReceived
import org.home.mvc.view.playerWasDisconnected
import org.home.mvc.view.readyPlayersReceived
import org.home.mvc.view.subscriptions
import org.home.net.action.ReadyAction
import org.home.style.AppStyles
import org.home.utils.extensions.BooleansExtensions.so
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.listview
import kotlin.reflect.KClass

class FleetGridCreationView : View("Создание флота") {

    internal val model: BattleModel by di()
    internal val applicationProperties: ApplicationProperties by di()
    
    internal val currentPlayer = applicationProperties.currentPlayer

    private val fleetGridCreationController: FleetGridCreationController by di()
    private val shipsTypesPaneController: ShipsTypesPaneController by di()
    internal val battleController: BattleController by di()

    private lateinit var playersListView: ListView<String>
    private lateinit var shipsTypesInfoPane: GridPane

    override val root = centerGrid()
    private val currentView = this@FleetGridCreationView

    init {
        primaryStage.setOnCloseRequest {
            battleController.onWindowClose()
        }

        val fleetState = fleetReadiness(model.battleShipsTypes)
        model.fleetsReadiness[currentPlayer] = fleetState

        title = currentPlayer.uppercase()

        model.playersReadiness.addValueListener {
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

        subscriptions {
            playerWasConnected()
            playerWasDisconnected(model)
            playerIsReadyReceived()
            playerIsNotReadyReceived()
            shipWasConstructed()
            shipWasDeleted()
            fleetsReadinessReceived()
            connectedPlayersReceived(model)
        }

        with(root) {
            row(0) {
                col(0) {
                    listview<String>(model.playersNames).also {
                        playersListView = it
                        it.cellFactory = MarkReadyPlayers(model)
                        subscriptions {
                            readyPlayersReceived(model, it)
                        }
                    }
                }
                col(1) { currentPlayerFleetGrid() }
                col(2) { currentPlayerShipsTypesInfoPane().apply { shipsTypesInfoPane = this } }
            }

            row(2) {
                col(0) {
                    backTransitButton(currentView, ::chooseViewToBack) {
                        battleController.onFleetCreationViewExit()
                    }
                }
                col(1) { clearFieldButton() }
                col(2) {
                    transitButton(currentView, BattleView::class, "Дальше") {
                        applicationProperties.isServer.so {
                            model.playersReadiness[currentPlayer] = true
                            battleController.send(ReadyAction(currentPlayer))
                        }
                    }
                }
            }
        }
    }

    private fun chooseViewToBack(): KClass<out View> =
        when {
            applicationProperties.isServer -> BattleCreationView::class
            else -> FleetGridCreationView::class
        }

    private fun EventTarget.currentPlayerFleetGrid() = fleetGridCreationController.root.also { add(it) }

    private fun EventTarget.currentPlayerShipsTypesInfoPane() =
        shipsTypesPaneController.shipTypesPane(currentPlayer).transpose().also {
            add(it)
            it.addClass(AppStyles.shipsTypesInfoPane)
        }

    private fun EventTarget.clearFieldButton() = button("Очистить") {
        action {
            model.playersAndShips[currentPlayer]!!.clear()
            TODO("FleetBoxController is needed that will handle an event of fleet grid clearance")
        }
    }
}

