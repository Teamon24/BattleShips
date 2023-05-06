package org.home.mvc.view.app

import javafx.scene.layout.VBox
import org.home.mvc.ApplicationProperties
import org.home.mvc.ApplicationProperties.Companion.creationButtonText
import org.home.mvc.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.StageUtils
import org.home.mvc.model.BattleModel
import org.home.mvc.view.Scopes
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.centerGrid
import org.home.mvc.view.components.transferTo
import org.home.mvc.view.inScope
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import java.awt.Dimension
import kotlin.math.roundToInt

class AppView : View("Sea Battle") {
    override val root = VBox()
    private val applicationProperties: ApplicationProperties by di()

    init {
        with(root) {
            addClass(AppStyles.form)
            centerGrid {
                cell(0, 0) {
                    button(creationButtonText) {
                        action {
                            Scopes.newGameScope()
                            BattleModel().inScope(Scopes.gameScope)
                            this@AppView.transferTo<BattleCreationView>()
                        }
                    }
                }

                cell(1, 0) {
                    button(joinButtonText) {
                        action {
                            Scopes.newGameScope()
                            BattleModel().inScope(Scopes.gameScope)
                            this@AppView.transferTo<BattleJoinView>()
                        }
                    }
                }
            }
        }

        applicationProperties.players?.also {
            val screenSize = StageUtils.screenSize()
            val shrink = 0.965
            StageUtils.setInitialPosition(this, applicationProperties.player!!, applicationProperties.players!!,
                {
                    screenSize.run { Dimension((width * shrink).roundToInt(), height) }
                }, {
                    this.x = this.x + screenSize.width * (1 - shrink)
                }
            )
        }
    }
}
