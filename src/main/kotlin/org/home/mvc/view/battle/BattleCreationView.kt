package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import org.home.app.di.GameScope
import org.home.mvc.AppView
import org.home.app.ApplicationProperties
import org.home.app.ApplicationProperties.Companion.battleFieldCreationMenuTitle
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.GameView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.marginGrid
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.backTransitButton
import org.home.mvc.view.component.button.battleButton
import org.home.mvc.view.component.button.exitButton
import org.home.mvc.view.component.transferTo
import org.home.mvc.view.openAlertWindow
import tornadofx.Form
import tornadofx.action

class BattleCreationView : GameView("Настройки боя") {
    private val shipsTypesPaneController: ShipsTypesPaneController by GameScope.inject()
    private val ip = applicationProperties.ip
    private val freePort = applicationProperties.port
    private val ipAddress = SimpleStringProperty("$ip:$freePort")

    private val battleController by di<BattleController<Action>>()
    private val settingsPaneController by GameScope.inject<SettingsPaneController>()

    override val root = Form().apply {
        title = battleFieldCreationMenuTitle
    }

    init {
        applicationProperties.isServer = true
        val currentView = this@BattleCreationView
        currentView.title = applicationProperties.currentPlayer.uppercase()
        with(root) {
            centerGrid {
                settingsPaneController {
                    cell(0, 0) { settingsPane() }
                }
                cell(1, 0) { shipsTypesPaneController.shipTypesPaneControl().also { add(it) } }
                cell(2, 0) {
                    marginGrid {
                        row(0) {
                            col(0) { backTransitButton<AppView>(currentView) }
                            col(1) { createBattleButton() }
                            col(2) { exitButton() }
                        }
                    }
                }
            }
        }
    }

    private fun EventTarget.createBattleButton() =
        battleButton(ApplicationProperties.createNewGameButtonText) {
            action {
                try {
                    battleController.connect("", freePort)
                    transferTo<BattleView>()
                } catch (e: Exception) {
                    e.printStackTrace()
                    openAlertWindow {
                        "Не удалось создать хост ${ipAddress.value}"
                    }
                }
            }
        }
}





