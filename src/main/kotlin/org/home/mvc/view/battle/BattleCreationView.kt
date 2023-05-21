package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import org.home.app.ApplicationProperties.Companion.battleFieldCreationMenuTitle
import org.home.app.di.gameScope
import org.home.mvc.AppView
import org.home.mvc.GameView
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.marginGrid
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.button.exitButton
import tornadofx.Form

class BattleCreationView : GameView("Настройки боя") {
    private val shipsTypesPaneController by gameScope<ShipsTypesPaneController>()
    private val settingsPaneController by gameScope<SettingsPaneController>()

    override fun onClose() { }

    override val root = Form().apply {
        title = battleFieldCreationMenuTitle
    }

    init {

        currentView().title = modelView.getCurrentPlayer().uppercase()
        with(root) {
            centerGrid {
                settingsPaneController   { cell(0, 0) { settingsPane() } }
                shipsTypesPaneController { cell(1, 0) { shipTypesPaneControl() } }
                cell(2, 0) {
                    marginGrid {
                        row(0) {
                            viewSwitchButtonController {
                                col(0) { backButton(currentView(), AppView::class) }
                                col(1) { createBattleButton(currentView()) }
                            }
                            col(2) { exitButton() }
                        }
                    }
                }
            }
        }
    }
}





