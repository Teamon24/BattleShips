package org.home.mvc.view

import javafx.scene.layout.VBox
import org.home.mvc.ApplicationProperties
import org.home.mvc.ApplicationProperties.Companion.creationButtonText
import org.home.mvc.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.StageUtils
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.transitButton
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.addClass
import java.awt.Dimension
import kotlin.math.roundToInt

class AppView : View("Hello TornadoFX") {

    private val model: BattleModel by di()
    private val appProps: ApplicationProperties by di()
    override val root = VBox()

    init {

        model.playersAndShips[appProps.currentPlayer] = mutableListOf()

        with(root) {
            addClass(AppStyles.form)
            centerGrid {
                cell(0, 0) {
                    transitButton(this@AppView, BattleCreationView::class, creationButtonText) {
                        appProps.isServer = true
                    }
                }

                cell(1, 0) {
                    transitButton(this@AppView, BattleJoinView::class, joinButtonText)
                }
            }
        }
        if (appProps.players != null) {
            val screenSize = StageUtils.screenSize()
            val shrink = 0.965
            StageUtils.setInitialPosition(this, appProps.player!!, appProps.players!!,
                {
                    screenSize.run { Dimension((width * shrink).roundToInt(), height) }
                }, {
                    this.x = this.x + screenSize.width * (1 - shrink)
                }
            )
        }
    }
}
