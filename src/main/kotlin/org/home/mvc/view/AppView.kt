package org.home.mvc.view

import javafx.scene.layout.VBox
import org.home.ApplicationProperties
import org.home.ApplicationProperties.Companion.creationButtonText
import org.home.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.StageUtils
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.transit
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.addClass

class AppView : View("Hello TornadoFX") {

    private val appProps: ApplicationProperties by di()
    override val root = VBox()

    init {

        with(root) {
            addClass(AppStyles.form)
            centerGrid {
                cell(0, 0) {
                    transit(this@AppView, BattleCreationView::class, creationButtonText) {
                        appProps.isServer = true
                    }
                }

                cell(1, 0) {
                    transit(this@AppView, BattleJoinView::class, joinButtonText)
                }
            }
        }
        if (appProps.players != null) {
            StageUtils.setInitialPosition(this, appProps.player!!, appProps.players!!, StageUtils::screenSize)
        }
    }
}
