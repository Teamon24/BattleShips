package org.home.mvc.view.component.button

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import org.home.app.ApplicationProperties.Companion.battleStartButtonTextForClient
import org.home.app.ApplicationProperties.Companion.battleStartButtonTextForServer
import org.home.app.ApplicationProperties.Companion.startButtonTransitionTime
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameComponent
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
import tornadofx.add
import tornadofx.hide
import tornadofx.style

class BattleStartButton(text: String = "") : Button(text)

class BattleStartButtonController : GameController() {
    private val battleController           by noScope<BattleController<Action>>()
    private val battleStartButtonComponent by gameScope<BattleStartButtonComponent>()

    private lateinit var button: BattleStartButton

    fun EventTarget.create(): BattleStartButton {
        return BattleStartButton().apply {
            battleStartButtonComponent {
                setButtonText()
                updateStyle()
            }

            action {
                log {"${bean.name}#${this.className} - is starting battle"}
                battleController.startBattle()
            }

            this@create.add(this)
            button = this
        }
    }

    fun updateStyle(player: String, ready: Boolean) {
        battleStartButtonComponent { button.updateStyle(player, ready) }
    }

    fun updateStyle(player: String) {
        battleStartButtonComponent {
            button.setButtonText()
            button.updateStyle(player)
        }
    }

    fun hide() {
        button.hide()
    }
}

abstract class BattleStartButtonComponent: GameComponent() {
    abstract fun BattleStartButton.updateStyle(player: String)
    abstract val buttonText: String

    fun BattleStartButton.setButtonText() {
        log { "${this@BattleStartButtonComponent.name} button text = $buttonText" }
        text = buttonText
    }

    fun BattleStartButton.updateStyle() {
        isDisable = true
        modelView.hasReady(currentPlayer) so { fillTransition() }
    }

    fun BattleStartButton.updateStyle(player: String, ready: Boolean) {
        if (currentPlayer == player) {
            ready
                .thus      { fillTransition() }
                .otherwise { reverseFillTransition() }
        }
        updateStyle(player)
    }

    protected fun BattleStartButton.fillTransition(onFinish: () -> Unit = {}) {
        fillTransition(initialAppColor, readyColor, BLACK, WHITE, onFinish)
    }

    protected fun BattleStartButton.reverseFillTransition(onFinish: () -> Unit = {}) {
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

class BattleStartButtonComponentForServer : BattleStartButtonComponent() {
    override val buttonText = battleStartButtonTextForServer
    override fun BattleStartButton.updateStyle(player: String) {
        log { "${bean.name}#updateStyle allAreReady = ${modelView.allAreReady}" }
        isDisable = !modelView.allAreReady
    }
}

class BattleStartButtonComponentForClient : BattleStartButtonComponent() {
    override val buttonText = battleStartButtonTextForClient
    override fun BattleStartButton.updateStyle(player: String) {
        if (currentPlayer != player) return
        when (modelView.hasReady(currentPlayer)) {
            true -> fillTransition()
            else -> reverseFillTransition()
        }
    }
}
