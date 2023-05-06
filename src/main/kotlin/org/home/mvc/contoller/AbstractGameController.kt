package org.home.mvc.contoller

import org.home.mvc.di.Injector.newGame
import org.home.mvc.model.BattleModel
import tornadofx.Component
import tornadofx.Controller
import tornadofx.ScopedInstance

abstract class AbstractGameController : Controller() {
    inline fun <reified T> newGame()
            where T : Component,
                  T : ScopedInstance = newGame<T>(this@AbstractGameController)

    protected val model: BattleModel by newGame()
    protected val applicationProperties = model.applicationProperties
    protected val currentPlayer = model.currentPlayer
}