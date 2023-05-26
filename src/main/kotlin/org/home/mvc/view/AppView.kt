package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import javafx.geometry.Pos
import org.home.app.ApplicationProperties.Companion.appViewAnimationGridHeight
import org.home.app.ApplicationProperties.Companion.appViewAnimationGridWidth
import org.home.mvc.Animations.appViewAnimationGrid
import org.home.mvc.GameView
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
                        appViewAnimationGrid(appViewAnimationGridWidth, appViewAnimationGridHeight)
                    }

                    cell(0, 0) {
                        centerGrid {
                            viewSwitchButtonController {
                                cell(0, 0) { serverNewGameButton(currentView()) }
                                cell(1, 0) { clientNewGameButton(currentView()) }
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
