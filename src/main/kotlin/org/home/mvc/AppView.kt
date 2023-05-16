package org.home.mvc

import javafx.event.EventTarget
import javafx.geometry.Pos
import org.home.mvc.view.Animations.appViewAnimationGrid
import org.home.app.di.GameScope
import org.home.mvc.ApplicationProperties.Companion.appViewAnimationGridHeight
import org.home.mvc.ApplicationProperties.Companion.appViewAnimationGridWidth
import org.home.mvc.ApplicationProperties.Companion.createNewGameButtonText
import org.home.mvc.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.view.AbstractGameView
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.PannableScrollPane.Companion.pannableScrollPane
import org.home.mvc.view.component.button.battleButton
import org.home.mvc.view.component.button.exitButton
import org.home.mvc.view.component.transferTo
import tornadofx.Form
import tornadofx.View
import tornadofx.action
import tornadofx.gridpane
import java.awt.Dimension
import kotlin.math.roundToInt

class AppView : View("Sea Battle") {

    override val root = Form()
    private val applicationProperties: ApplicationProperties by di()

    init {

        with(root) {
            alignment = Pos.CENTER
            pannableScrollPane {
                content = gridpane {

                    cell(0, 0) {
                        appViewAnimationGrid(
                            appViewAnimationGridWidth,
                            appViewAnimationGridHeight
                        ).also {
                            add(it)
                        }
                    }

                    cell(0, 0) {
                        centerGrid {
                            cell(0, 0) { newGameButton<BattleCreationView>(createNewGameButtonText) }
                            cell(1, 0) { newGameButton<BattleJoinView>(joinButtonText) }
                            cell(2, 0) { exitButton() }
                        }.apply {
                            toFront()
                        }
                    }
                }
            }
        }

        applicationProperties.players?.also {
            val screenSize = StageUtils.screenSize()
            val shrink = 0.965
            StageUtils.setInitialPosition(
                this,
                applicationProperties.player!!,
                applicationProperties.players!!,
                { screenSize.run { Dimension((width * shrink).roundToInt(), height) } },
                { this.x = this.x + screenSize.width * (1 - shrink) }
            )
        }
    }

    private inline fun <reified T: AbstractGameView> EventTarget.newGameButton(text: String) = battleButton(text) {
        action {
            GameScope.createNew()
            this@AppView.transferTo<T>()
        }
    }
}
