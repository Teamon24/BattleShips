package org.home.mvc.view.component.button

import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.scene.control.Button
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import org.home.mvc.ApplicationProperties.Companion.startButtonTransitionTime
import org.home.mvc.contoller.AbstractGameBean
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

class BattleStartButtonController : AbstractGameBean() {
    private val battleController: BattleController<Action> by di()

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
        if (applicationProperties.isServer) {
            model.hasReady(currentPlayer) so { fillTransition() }
        } else {
            when (model.hasReady(currentPlayer)) {
                true -> fillTransition()
                else -> reverseFillTransition()
            }
        }
    }

    fun BattleStartButton.updateStyle(player: String, isReady: Boolean) {
        currentPlayer.equals(player) so {
            isReady thus { fillTransition() } otherwise { reverseFillTransition() }
        }

        if (applicationProperties.isServer) {
            isDisable = !model.allAreReady
        } else {
            if (currentPlayer != player) return

            when (model.hasReady(currentPlayer)) {
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