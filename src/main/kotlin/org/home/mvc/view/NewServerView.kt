package org.home.mvc.view

import javafx.scene.Parent
import javafx.scene.layout.VBox
import org.home.mvc.contoller.BattleController
import org.home.net.message.Action

class NewServerView(override val root: Parent = VBox()) : AbstractGameView("Перенос сервера") {
    internal val battleController: BattleController<Action> by di()
}