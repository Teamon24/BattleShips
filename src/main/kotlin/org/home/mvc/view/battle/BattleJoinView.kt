package org.home.mvc.view.battle

import home.extensions.AnysExtensions.invoke
import org.home.app.ApplicationProperties.Companion.ipAddressFieldLabel
import org.home.mvc.AppView
import org.home.mvc.GameView
import org.home.mvc.view.component.GridPaneExtensions.cell
import org.home.mvc.view.component.GridPaneExtensions.centerGrid
import org.home.mvc.view.component.GridPaneExtensions.col
import org.home.mvc.view.component.GridPaneExtensions.row
import tornadofx.label
import tornadofx.textfield

class BattleJoinView : GameView("Присоединиться к битве") {

    init {
        this.title = applicationProperties.currentPlayer.uppercase()
    }

    override fun onClose() {}

    override val root = centerGrid {
        row(0) {
            col(0) { label(ipAddressFieldLabel) }
            col(1) { textfield(viewSwitchButtonController.ipAddress) }
        }

        viewSwitchButtonController {
            cell(1, 1) { joinBattleButton(currentView()) }
            cell(2, 1) { backButton(currentView(), AppView::class) }
        }
    }


}



