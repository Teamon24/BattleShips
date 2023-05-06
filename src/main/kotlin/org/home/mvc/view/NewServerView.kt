package org.home.mvc.view

import javafx.scene.Parent
import javafx.scene.layout.VBox
import org.home.mvc.contoller.BattleController

class NewServerView(override val root: Parent = VBox()) : AbstractGameView("Перенос сервера") {
    internal val battleController: BattleController by di()
}