package org.home.mvc.contoller

import org.home.app.AbstractApp.Companion.newGame
import org.home.mvc.model.BattleModel
import tornadofx.Controller

abstract class AbstractGameBean : Controller() {

    protected val model: BattleModel by newGame()

    protected val applicationProperties = model.applicationProperties
    protected open val currentPlayer = model.currentPlayer
}