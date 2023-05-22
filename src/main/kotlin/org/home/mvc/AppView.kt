package org.home.mvc

import home.extensions.AnysExtensions.invoke
import javafx.geometry.Pos
import org.home.app.ApplicationProperties.Companion.appViewAnimationGridHeight
import org.home.app.ApplicationProperties.Companion.appViewAnimationGridWidth
import org.home.app.ApplicationProperties.Companion.connectionButtonText
import org.home.app.ApplicationProperties.Companion.createNewGameButtonText
import org.home.app.di.GameScope
import org.home.mvc.view.Animations.appViewAnimationGrid
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.PannableScrollPane.Companion.pannableScrollPane
import org.home.mvc.view.component.button.ViewSwitchButtonController
import org.home.mvc.view.component.button.exitButton
import tornadofx.Form
import tornadofx.gridpane

class AppView : GameView("Sea Battle") {

    override fun onClose() {}

    override val root = Form()

    init {

        with(root) {
            alignment = Pos.CENTER
            pannableScrollPane {
                content = gridpane {

                    cell(0, 0) {
                        appViewAnimationGrid(appViewAnimationGridWidth, appViewAnimationGridHeight)
                    }

                    cell(0, 0) {
                        centerGrid {
                            viewSwitchButtonController {
                                cell(0, 0) {
                                    newGameButton<BattleCreationView>(currentView(), createNewGameButtonText) {
                                        setServerNewGame(true)
                                    }
                                }
                                cell(1, 0) {
                                    newGameButton<BattleJoinView>(currentView(), connectionButtonText) {
                                        setServerNewGame(false)
                                    }
                                }
                            }
                            cell(2, 0) { exitButton() }
                        }.apply {
                            toFront()
                        }
                    }
                }
            }
        }
    }

    private inline fun ViewSwitchButtonController.setServerNewGame(b: Boolean) {
        applicationProperties.isServer = b
        GameScope.createNew()
    }
}
