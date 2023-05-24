package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.home.app.ApplicationProperties.Companion.enemySelectionFadeTime
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameComponent
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPane
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.openAlertWindow
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.defeatedPlayerColor
import org.home.style.AppStyles.Companion.noColor
import org.home.style.TimelineFadeTransitions.fadeIn
import org.home.style.TimelineFadeTransitions.fadeOut
import org.home.style.TimelineFadeTransitions.fadeOver
import org.home.utils.NodeUtils.disable
import org.home.utils.StyleUtils.rightPadding
import org.home.utils.log
import tornadofx.onLeftClick
import tornadofx.selectedItem
import tornadofx.style

typealias PlayersAndFleets = LinkedHashMap<String, FleetGrid>
typealias PlayersAndFleetsReadiness = LinkedHashMap<String, ShipsTypesPane>

class EnemiesViewController : GameComponent() {
    companion object {
        private val fadeTime = enemySelectionFadeTime.toDouble()
    }

    internal val fleetGridsPanes = PlayersAndFleets()
    internal val fleetsReadinessPanes = PlayersAndFleetsReadiness()

    private val fleetGridController by gameScope<FleetGridController>()
    private val shipsTypesPaneController by gameScope<ShipsTypesPaneController>()
    private val battleController by noScope<BattleController<Action>>()

    private val enemiesListViewController by gameScope<EnemiesListViewController>()

    private fun EnemiesViewController.notNulls(vararg anys: Any?) = anys.all { it != null }

    init {
        initByFirstIfPresent()
        enemiesListViewController.onChange { _, old, new ->
            when {
                new == old -> return@onChange
                old == null && new != null    -> fadeOutFirst(new)
                notNulls(old, new)            -> fadeOverNext(new!!)
                old != null && new == null    -> fadeInLast()
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
            fadeOutFirst(it)
        }
    }

    fun remove(player: String) {
        fleetGridsPanes.remove(player)
        fleetsReadinessPanes.remove(player)
        modelView.remove(player)
    }

    private fun fadeInLast() {
        fadeIn(fadeTime, selectedEnemyLabel) { selectedEnemyLabel.text = "" }
        fadeIn(fadeTime, selectedFleetPane) { selectedFleetPane.center = null }
        fadeIn(fadeTime, selectedFleetReadinessPane) { selectedFleetReadinessPane.center = null }
    }

    private fun fadeOverNext(selected: String) {
        log { "set to panes: $selected" }

        fadeOverLabel(selected)
        fadeOverFleet(selected)
        fadeOverReadiness(selected)
    }

    private fun fadeOutFirst(player: String) {
        selectedEnemyLabel {
            fadeOut(fadeTime, this) { text = player }
        }

        selectedFleetPane {
            assign(fleetGridsPanes[player]!!)
            fadeOut(fadeTime, center)
        }

        selectedFleetReadinessPane {
            assign(fleetsReadinessPanes[player]!!)
            fadeOut(fadeTime, center)
        }
    }

    private fun fadeOverLabel(selected: String) {
        selectedEnemyLabel.also {
            fadeOver(fadeTime, it) { _ ->
                it.text = selected
                it.style(append = true) {
                    backgroundColor += modelView
                        .hasDefeated(selected)
                        .then(defeatedPlayerColor)
                        .or(noColor)
                }
            }
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
            getEnemies().firstOrNull()?.also { player ->
                selectedFleetPane.center ?: kotlin.run {
                    fadeOutFirst(player)
                    playersNodes[player]!!.apply { afterInit() }
                }
            }
        }
    }
}