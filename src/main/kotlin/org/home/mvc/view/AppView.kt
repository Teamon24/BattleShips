package org.home.mvc.view

import org.home.app.ApplicationProperties.Companion.creationButtonText
import org.home.app.ApplicationProperties.Companion.joinButtonText
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.centerGrid
import org.home.mvc.view.components.transit
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.addClass
import tornadofx.vbox

class AppView : View("Hello TornadoFX") {
    override val root = vbox {

        addClass(AppStyles.form)
        centerGrid {
            cell(0, 0) {
                transit(this@AppView, BattleCreationView::class, creationButtonText)
            }

            cell(1, 0) {
                transit(this@AppView, BattleJoinView::class, joinButtonText)
            }
        }
    }
}
