package org.home.mvc.view

import javafx.animation.FillTransition
import javafx.animation.Interpolator
import javafx.animation.TranslateTransition
import javafx.scene.Parent
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.util.Duration
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.subscriptions.subscriptions
import org.home.mvc.view.components.transferTo
import org.home.net.message.Action
import org.home.style.AppStyles
import org.home.utils.logEvent


class NewServerView(override val root: Parent = GridPane()) : AbstractGameView("Перенос сервера") {
    internal val battleController: BattleController<Action> by di()

    override fun exit() {
        battleController.disconnect()
        super.exit()
    }

    init {
        val fade = FillTransition()
        fade.duration = Duration.millis(2000.0)
        fade.cycleCount = TranslateTransition.INDEFINITE
        fade.interpolator = Interpolator.LINEAR
        fade.fromValue = Color.WHITE
        fade.toValue = AppStyles.chosenCellColor
        fade.play()
        subscriptions {
            serverTransferClientsReceived()
        }
    }
}

internal fun NewServerView.serverTransferClientsReceived() {
    subscribe<NewServerConnectionReceived> {
        logEvent(it, model)
        battleController.disconnect()
        battleController.connect(it.action.ip, it.action.port)
        transferTo<BattleView>()
    }
}