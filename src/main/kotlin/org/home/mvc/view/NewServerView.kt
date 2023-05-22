package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import javafx.scene.Parent
import javafx.scene.layout.VBox
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.contoller.serverTransfer.NewServerViewController
import java.util.*

class NewServerView(override val root: Parent = VBox()) : GameView() {
    init {
        title = "${modelView.getCurrentPlayer().uppercase()}: перенос сервера"
    }

    fun withRoot(block: Parent.() -> Unit) = root.block()

    internal val battleController by noScope<BattleController<Action>>()
    private val newServerViewController by gameScope<NewServerViewController>()
    internal var threadIndicator: Thread? = null
    internal val connectedPlayers =
        Collections
            .synchronizedList(modelView.getPlayers().toMutableList())
            .apply { remove(modelView.getNewServer().player) }


    override fun onClose() {
        battleController.leaveBattle()
        battleController.disconnect()
    }

    init {
        newServerViewController { subscribe() }
        newServerViewController { init() }
    }
}
