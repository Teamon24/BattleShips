package org.home.mvc.view

import javafx.scene.Parent
import javafx.scene.layout.VBox
import org.home.mvc.contoller.BattleController
import tornadofx.View

class NewServerView(override val root: Parent = VBox()) : View("Перенос сервера") {
    internal val battleController: BattleController by di()
}