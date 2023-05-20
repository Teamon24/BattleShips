package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.isAny
import home.extensions.BooleansExtensions.so
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.home.app.di.GameScope
import org.home.app.ApplicationProperties.Companion.enemySelectionFadeTime
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameComponent
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.openAlertWindow
import org.home.style.AppStyles
import org.home.style.TimelineFadeTransitions.fadeIn
import org.home.style.TimelineFadeTransitions.fadeOut
import org.home.style.TimelineFadeTransitions.fadeOver
import org.home.utils.NodeUtils.disable
import org.home.utils.StyleUtils.rightPadding
import org.home.utils.log
import tornadofx.onLeftClick
import tornadofx.selectedItem

class EnemiesViewController : GameComponent() {
    companion object {
        private val fadeTime = enemySelectionFadeTime.toDouble()
    }

    internal val fleetGridsPanes = hashMapOf<String, FleetGrid>()
    internal val fleetsReadinessPanes = hashMapOf<String, ShipsTypesPane>()

    private val fleetGridController by GameScope.inject<FleetGridController>()
    private val shipsTypesPaneController by GameScope.inject<ShipsTypesPaneController>()
    private val battleController by di<BattleController<Action>>()

    private val enemiesListViewController by GameScope.inject<EnemiesListViewController>()

    private fun EnemiesViewController.notNulls(vararg anys: Any?) = anys.all { it != null }

    init {
        initByFirstIfPresent()
        enemiesListViewController.onSelect { currentPlayer, old, new ->
            when {
                new.isAny(old, currentPlayer) -> return@onSelect
                notNulls(old, new)            -> fadeOver(new!!)
                old == null && new != null    -> fadeOut(new)
                old != null && new == null    -> fadeIn()
            }
            log { "selected: $selectedItem" }
        }
    }

    val enemiesList = enemiesListViewController.view

    private val selectedItem get() = enemiesList.selectedItem

    val selectedEnemyLabel = Label()
    val selectedFleetPane = BorderPane()
    val selectedFleetReadinessPane = BorderPane()

    fun refresh() {
        enemiesList.refresh()
    }

    private fun initByFirstIfPresent() {
        selectedFleetPane { initByFirstIfPresent(fleetGridsPanes) { disable() } }
        selectedFleetReadinessPane.initByFirstIfPresent(fleetsReadinessPanes)
    }

    fun add(connectedPlayer: String) {
        modelView.add(connectedPlayer)
        fleetGridsPanes[connectedPlayer] = enemyFleetGrid().disable()
        fleetsReadinessPanes[connectedPlayer] = enemyFleetReadinessPane(connectedPlayer).disable()
        enemiesListViewController.selectIfFirst(connectedPlayer) {
            fadeOut(it)
        }
    }

    fun remove(player: String) {
        fleetGridsPanes.remove(player)
        fleetsReadinessPanes.remove(player)
        modelView.remove(player)
    }

    private fun fadeIn() {
        fadeIn(fadeTime, selectedEnemyLabel) { selectedEnemyLabel.text = "" }
        fadeIn(fadeTime, selectedFleetPane) { selectedFleetPane.center = null }
        fadeIn(fadeTime, selectedFleetReadinessPane) { selectedFleetReadinessPane.center = null }
    }

    private fun fadeOver(selected: String) {
        log { "set to panes: $selected" }

        fadeOverLabel(selected)
        fadeOverFleet(selected)
        fadeOverReadiness(selected)
    }

    private fun fadeOut(player: String) {
        selectedEnemyLabel {
            fadeOut(fadeTime, this) { text = player }
        }

        selectedFleetPane {
            assign(fleetGridsPanes[player]!!)
            fadeOut(fadeTime, center) { assign(fleetGridsPanes[player]!!) }
        }

        selectedFleetReadinessPane {
            assign(fleetsReadinessPanes[player]!!)
            fadeOut(fadeTime, center) { assign(fleetsReadinessPanes[player]!!) }
        }
    }

    private fun fadeOverLabel(selected: String) {
        selectedEnemyLabel.also {
            fadeOver(fadeTime, it) { _ -> it.text = selected }
        }
    }

    private fun fadeOverFleet(selected: String) {
        val fleetGrid = fleetGridsPanes[selected]!!
        selectedFleetPane {
            fadeOver(fadeTime, center, fleetGrid) { assign(fleetGrid) }
        }
    }

    private fun fadeOverReadiness(selected: String) {
        val fleetReadiness = fleetsReadinessPanes[selected]!!
        selectedFleetReadinessPane {
            fadeOver(fadeTime, center, fleetReadiness) { assign(fleetReadiness) }
        }
    }

    private fun <T: Node> BorderPane.assign(center: T) { this.center = center }

    private fun enemyFleetReadinessPane(player: String) =
        shipsTypesPaneController.shipTypesPane(player)
            .transposed()
            .flip()
            .apply { rightPadding(10) }


    private fun enemyFleetGrid() =
        fleetGridController
            .fleetGrid()
            .addFleetCellClass(AppStyles.enemyCell)
            .onEachFleetCells {
                it.onLeftClick {
                    val enemyToHit = selectedItem
                    if (enemyToHit == null) {
                        openAlertWindow { "Выберите игрока для выстрела" }
                        return@onLeftClick
                    }

                    val hitCoord = it.coord
                    log { "shooting $hitCoord" }

                    modelView.hasNo(enemyToHit, hitCoord).so {
                        battleController.shot(enemyToHit, hitCoord)
                    }
                }
            }

    private inline fun <N : Node> BorderPane.initByFirstIfPresent(
        playersNodes: Map<String, N>,
        afterInit: N.() -> Unit = {},
    ) {
        modelView {
            enemies.firstOrNull()?.also { player ->
                fadeOut(player)
                playersNodes[player]!!.apply { afterInit() }
            }
        }
    }
}