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

class BattleViewExitButtonController: GameController() {
    private lateinit var button: Button

    val row = 2
    val col = 0
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

    fun EventTarget.setDefeated(battleView: BattleView): Button {
        viewSwitchButtonController { button = defeatedLeaveButton(battleView) }
        return button
    }
}
