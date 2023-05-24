package org.home.mvc.view.component.button

import home.extensions.AnysExtensions.invoke
import javafx.event.EventTarget
import javafx.scene.control.Button
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameController
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.AppView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.component.GridPaneExtensions.cell

class BattleViewExitButtonController: GameController() {

    private lateinit var button: Button

    val row = 2
    val indices = row to 0
    private val viewSwitchButtonController by gameScope<ViewSwitchButtonController>()
    private val battleController by noScope<BattleController<Action>>()

    fun EventTarget.create(battleView: BattleView) {
        indices {
            viewSwitchButtonController {
                cell(first, second) { this@create.leaveButton(battleView).also { button = it } }
            }
        }
    }

    fun setText(text: String) {
        button.text = text
    }

    fun setTransit(battleView: BattleView) {
        viewSwitchButtonController.setTransit<AppView>(button, battleView) {
            battleController.leaveBattle()
        }
    }

    fun EventTarget.setDefeated(battleView: BattleView) {
        viewSwitchButtonController {
            cell(indices.first, indices.second) {
                defeatedLeaveButton(battleView).also { button = it }
            }
        }
    }
}
