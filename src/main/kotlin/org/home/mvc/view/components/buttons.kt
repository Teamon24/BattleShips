package org.home.mvc.view.components

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.yes
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Button
import org.home.mvc.AppView
import org.home.mvc.ApplicationProperties.Companion.exitText
import org.home.mvc.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.mvc.model.notAllReady
import org.home.mvc.view.AbstractGameView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.GridPaneExtensions.cell
import org.home.mvc.view.components.GridPaneExtensions.getIndices
import org.home.style.AppStyles
import org.home.style.CssUtils.hover
import org.home.style.HoverTransition
import tornadofx.action
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.button
import tornadofx.removeClass
import kotlin.system.exitProcess

inline fun EventTarget.exitButton(view: AbstractGameView) =
    battleButton(exitText) {
        action {
            view.exit()
        }
    }

inline fun EventTarget.exitButton() =
    battleButton(exitText) {
        action {
            exitProcess(0)
        }
    }

fun EventTarget.battleButton(text: String = "", graphic: Node? = null, op: Button.() -> Unit = {}) =
    BattleButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
    }

class BattleButton(text: String): Button(text) {
    var hoverTransition: HoverTransition? = null

    fun disableHover() {
        hoverTransition?.disable()
        hoverTransition = null
    }

    init { hover() }
}

fun EventTarget.battleStartButton(text: String = "", graphic: Node? = null, op: BattleStartButton.() -> Unit = {}) =
    BattleStartButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
    }

class BattleStartButton(text: String): Button(text) {

    fun updateStyle(view: AbstractGameView) {
        view {
            if (applicationProperties.isServer) {
                isDisable = model.notAllReady
                    .yes { removeClass(AppStyles.readyButton) }
                    .no { addClass(AppStyles.readyButton) }
            } else {
                when (model.hasReady(currentPlayer)) {
                    true -> addClass(AppStyles.readyButton)
                    else -> removeClass(AppStyles.readyButton)
                }
            }
        }
    }
}
