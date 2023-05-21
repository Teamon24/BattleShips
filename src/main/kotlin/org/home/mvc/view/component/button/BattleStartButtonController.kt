package org.home.mvc.view.component.button

import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.control.Button
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import org.home.app.ApplicationProperties.Companion.startButtonTransitionTime
import org.home.app.di.noScope
import org.home.mvc.GameController
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.model.allAreReady
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.AppStyles.Companion.readyColor
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import org.home.utils.log
import tornadofx.action
import tornadofx.style

class BattleStartButton(text: String) : Button(text)

class BattleStartButtonController : GameController() {
    private val battleController by noScope<BattleController<Action>>()

    fun create(): BattleStartButton {
        val text = if (applicationProperties.isServer) "В бой" else "Готов"
        return BattleStartButton(text).apply {
            updateStyle()
            action {
                log { "battleController: ${battleController.name}" }
                battleController.startBattle()
            }
        }
    }

    private fun BattleStartButton.updateStyle() {
        isDisable = true
        modelView.hasReady(currentPlayer) so { fillTransition() }
    }

    fun BattleStartButton.updateStyle(player: String, ready: Boolean) {
        if (currentPlayer == player) {
            ready thus { fillTransition() } otherwise { reverseFillTransition() }
        }

        if (applicationProperties.isServer) {
            isDisable = !modelView.allAreReady
        } else {
            if (currentPlayer != player) return

            when (modelView.hasReady(currentPlayer)) {
                true -> fillTransition()
                else -> reverseFillTransition()
            }
        }
    }

    private fun BattleStartButton.fillTransition(onFinish: () -> Unit = {}) {
        fillTransition(initialAppColor, readyColor, BLACK, WHITE, onFinish)
    }

    private fun BattleStartButton.reverseFillTransition(onFinish: () -> Unit = {}) {
        fillTransition(readyColor, initialAppColor, WHITE, BLACK, onFinish)
    }

    private fun BattleStartButton.fillTransition(
        from: Color,
        to: Color,
        textFrom: Color,
        textTo: Color,
        onFinish: () -> Unit = {}
    ) {
        style {
            filling(this@fillTransition) {
                millis = startButtonTransitionTime
                transition(from, to) { backgroundColor += it }
                transition(textFrom, textTo) { textFill = it }
                onFinish(onFinish)
            }
        }
    }
}