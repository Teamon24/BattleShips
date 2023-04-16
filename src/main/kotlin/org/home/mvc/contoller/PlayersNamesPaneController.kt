package org.home.mvc.contoller

import javafx.scene.control.ListView
import org.home.app.injecting
import org.home.mvc.model.BattleModel
import tornadofx.Controller

class PlayersNamesPaneController: Controller() {
    private val model: BattleModel by injecting()
    private val playersNames = model.playersNames
    val playersListView = ListView(playersNames).apply {
        isMouseTransparent = true
        setFocusTraversable(false)
    }

    init {

        subscribe<PlayerWasConnected> {
            playersNames.add(it.playerName)
        }

        subscribe<PlayerWasDisconnected> {
            playersNames.remove(it.playerName)
        }
    }
}
