package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import javafx.geometry.Pos
import org.home.app.ApplicationProperties
import org.home.app.ApplicationProperties.Companion.appViewAnimationCellSize
import org.home.app.ApplicationProperties.Companion.appViewAnimationGridRows
import org.home.app.ApplicationProperties.Companion.appViewAnimationGridColumns
import org.home.app.ApplicationProperties.Companion.createNewGameButtonText
import org.home.app.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.Animations.appViewAnimationGrid
import org.home.mvc.GameView
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.PannableScrollPane.Companion.pannableScrollPane
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
                        appViewAnimationGrid(
                            appViewAnimationGridColumns,
                            appViewAnimationGridRows,
                            appViewAnimationCellSize,
                            ApplicationProperties.appViewAnimationTime
                        )
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
                                    newGameButton<BattleJoinView>(currentView(), joinButtonText) {
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
}
