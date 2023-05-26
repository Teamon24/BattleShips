package org.home.mvc.view.component.button

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.event.EventTarget
import javafx.scene.control.Button
import org.home.app.ApplicationProperties.Companion.backButtonText
import org.home.app.ApplicationProperties.Companion.buttonHoverTransitionTime
import org.home.app.ApplicationProperties.Companion.connectionButtonText
import org.home.app.ApplicationProperties.Companion.createNewGameButtonText
import org.home.app.ApplicationProperties.Companion.joinButtonText
import org.home.app.ApplicationProperties.Companion.leaveBattleFieldButtonTransitionTime
import org.home.app.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.app.di.GameScope
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameView
import org.home.mvc.ViewSwitchController
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.server.AddressComponent
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.AppView
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleJoinView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.component.NextClass
import org.home.mvc.view.component.Prev
import org.home.mvc.view.component.ViewSwitch
import org.home.mvc.view.openAlertWindow
import org.home.style.AppStyles.Companion.defeatedColor
import org.home.style.AppStyles.Companion.initialAppColor
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.hovering
import org.home.style.TransitionDSL.transition
import org.home.utils.StyleUtils.textFillTransition
import tornadofx.View
import tornadofx.action
import tornadofx.style

object ViewSwitchButtonController: ViewSwitchController() {
    private val addressComponent by gameScope<AddressComponent>()
    internal val address by lazy { addressComponent.getAddress() }
    private val battleController by noScope<BattleController<Action>>()

    fun getIpPort(): Pair<String, Int> {
        val ip = address.split(":")[0]
        val port = address.split(":")[1]
        return (applicationProperties.isServer then "" or ip) to port.toInt()
    }

    fun EventTarget.serverNewGameButton(appView: AppView): BattleButton =
        newGameButton<BattleCreationView>(appView, createNewGameButtonText) { GameScope.createNew() }

    fun EventTarget.clientNewGameButton(appView: AppView): BattleButton =
        newGameButton<BattleJoinView>(appView, joinButtonText) { GameScope.createNew() }


    inline fun <reified T : View> setTransit(
        button: Button,
        from: Prev,
        crossinline onBefore: () -> Unit = {}
    ) {
        button.action {
            viewSwitch {
                onBefore()
                from.transitTo(T::class)
            }
        }
    }

    inline fun <T : View> EventTarget.backButton(
        from: View,
        to: NextClass<T>,
        text: String = backButtonText,
        noHover: Boolean = false,
        crossinline transit: ViewSwitch.(Prev, NextClass<T>) -> Unit = { prev, next -> prev.backTransitTo(next) },
        crossinline onActionStart: () -> Unit = {}
    ) =
        battleButton(text, noHover = noHover) {
            action {
                onActionStart()
                viewSwitch.transit(from, to)
            }
        }

    private inline fun <reified T : GameView> EventTarget.newGameButton(
        from: View,
        text: String,
        crossinline onActionStart: () -> Unit
    ) =
        battleButton(text) {
            action {
                onActionStart()
                viewSwitch {
                    from.transferTo(T::class)
                }
            }
        }

    fun EventTarget.createBattleButton(battleCreationView: BattleCreationView) =
        battleButtonLogic(
            createNewGameButtonText,
            battleCreationView,
            getIpPort(),
            "Не удалось создать хост $address"
        )

    fun EventTarget.connectBattleButton(battleJoinView: BattleJoinView) =
        battleButtonLogic(
            connectionButtonText,
            battleJoinView,
            getIpPort(),
            "Не удалось подключиться к хосту $address"
        )

    fun EventTarget.leaveButton(battleView: BattleView) =
        backButton(battleView, AppView::class) {
            battleController.leaveBattle()
        }

    fun EventTarget.defeatedLeaveButton(battleView: BattleView) =
        backButton(battleView, AppView::class, leaveBattleFieldText, noHover = true) {
            battleController.leaveBattle()
        }.apply {
            style {
                val battleButton = this@apply
                filling(battleButton) {
                    millis = leaveBattleFieldButtonTransitionTime
                    transition(initialAppColor, defeatedColor) { backgroundColor += it }
                    textFillTransition()
                }
                hovering(battleButton) {
                    millis = buttonHoverTransitionTime
                    transition(defeatedColor, defeatedColor.darker()) { backgroundColor += it }
                }
            }
        }


    private fun EventTarget.battleButtonLogic(name: String, from: View, ipPort: Pair<String, Int>, message: String) =
        battleButton(name) {
            action {
                try {
                    battleController.connect(ipPort.first, ipPort.second)
                    viewSwitch { from.transferTo(BattleView::class) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    openAlertWindow { message }
                }
            }
        }


}