package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import org.home.app.di.GameScope
import org.home.mvc.AppView
import org.home.app.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.view.GameView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.row
import org.home.mvc.view.component.backTransitButton
import org.home.mvc.view.component.button.BattleConnectionButtonController
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : GameView("Присоединиться к битве") {

    private val battleConnectionButtonController by GameScope.inject<BattleConnectionButtonController>()

    private val currentView = this@BattleJoinView

    init {
        applicationProperties.isServer = false
        this.title = applicationProperties.currentPlayer.uppercase()
    }

    override val root = centerGrid {
        row(0) {
            col(0) { label(ipAddressFieldLabel) }
            col(1) { textfield(battleConnectionButtonController.ipAddress) }
        }

        battleConnectionButtonController {
            cell(1, 1) { create(currentView) }
        }
        cell(2, 1) { backTransitButton<AppView>(currentView) }
    }


}



