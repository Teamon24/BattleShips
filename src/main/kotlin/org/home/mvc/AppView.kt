package org.home.mvc

import javafx.event.EventTarget
import javafx.scene.layout.VBox
import org.home.app.Animations.appViewAnimationGrid
import org.home.app.di.Scopes
import org.home.app.di.Scopes.inScope
import org.home.mvc.ApplicationProperties.Companion.appViewAnimationGridSize
import org.home.mvc.ApplicationProperties.Companion.createNewGameButtonText
import org.home.mvc.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.model.BattleModel
import org.home.mvc.view.AbstractGameView
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.PannableScrollPane.Companion.pannableScrollPane
import org.home.mvc.view.components.battleButton
import org.home.mvc.view.components.exitButton
import org.home.mvc.view.components.transferTo
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.gridpane
import java.awt.Dimension
import kotlin.math.roundToInt

class AppView : View("Sea Battle") {

    override val root = VBox()
    private val applicationProperties: ApplicationProperties by di()

    init {
        with(root) {
            addClass(AppStyles.form)
            pannableScrollPane {
                content = gridpane {

                    cell(0, 0) {
                        appViewAnimationGrid(appViewAnimationGridSize).also { add(it); }
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
            Scopes.newGameScope()
            BattleModel().inScope(Scopes.gameScope)
            this@AppView.transferTo<T>()
        }
    }
}
