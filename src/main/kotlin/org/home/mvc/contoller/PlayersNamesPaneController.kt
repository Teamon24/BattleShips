package org.home.mvc.contoller

import javafx.scene.control.ListView

import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.model.BattleModel
import tornadofx.Controller

class PlayersNamesPaneController: Controller() {
    private val model: BattleModel by di()
    private val playersNames = model.playersNames
    val playersListView = ListView(playersNames).apply {
        isMouseTransparent = true
        setFocusTraversable(false)
    }
}
