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

class BattleViewExitButtonController(val row: Int = 2, val col: Int = 0) : GameController() {
    private lateinit var button: Button
    val indices = row to col
    private val viewSwitchButtonController by gameScope<ViewSwitchButtonController>()
    private val battleController by noScope<BattleController<Action>>()

    fun EventTarget.create(battleView: BattleView): Button {
        val eventTarget = this@create
        viewSwitchButtonController { button = eventTarget.leaveButton(battleView) }
        return button
    }

    fun setText(text: String) {
        button.text = text
    }

    fun setTransit(battleView: BattleView) {
        viewSwitchButtonController.setTransit<AppView>(button, battleView) {
            battleController.leaveBattle()
        }
    }

    fun BattleView.setDefeated(eventTarget: EventTarget): Button {
        viewSwitchButtonController { button = eventTarget.defeatedLeaveButton(this@setDefeated) }
        return button
    }

    fun setToCell(defeated: Button) {
        cell(row, col) { defeated }
    }
}
