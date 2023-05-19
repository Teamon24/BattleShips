package org.home.mvc.contoller

import org.home.app.di.GameScope
import org.home.mvc.model.BattleModel
import tornadofx.Component
import tornadofx.ScopedInstance

abstract class GameBean: Component(), ScopedInstance

abstract class GameComponent: GameBean() {
    protected val model: BattleModel by GameScope.inject()
    protected val applicationProperties = model.applicationProperties
    protected open val currentPlayer = model.currentPlayer
}

abstract class GameController : GameComponent()
