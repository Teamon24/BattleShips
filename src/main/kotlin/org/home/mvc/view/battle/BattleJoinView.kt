package org.home.mvc.view.battle

import org.home.app.ApplicationProperties
import org.home.app.ApplicationProperties.Companion.ipFieldLabel
import org.home.app.injecting
import org.home.mvc.view.AppView
import org.home.mvc.view.components.backTransit
import org.home.mvc.view.components.cell
import org.home.mvc.view.components.col
import org.home.mvc.view.components.row
import org.home.mvc.view.components.transit
import org.home.mvc.view.fleet.FleetCreationView
import org.home.style.AppStyles
import tornadofx.View
import tornadofx.addClass
import tornadofx.gridpane
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : View("Присоединиться к битве") {

    private val applicationProperties: ApplicationProperties by injecting()

    override val root = gridpane {
        addClass(AppStyles.form)
        row(0) {
            col(0) { label(ipFieldLabel).apply { addClass(AppStyles.fieldSize) } }
            col(1) { textfield() }
        }

        cell(1, 1) {
            transit(this@BattleJoinView, FleetCreationView::class, "Подключиться") {
                applicationProperties["isServer"] = false
            }
        }

        cell(2, 1) {
            backTransit(this@BattleJoinView, AppView::class)
        }
    }
}
