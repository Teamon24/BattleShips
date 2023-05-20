package org.home.mvc.contoller

import org.home.app.di.GameScope
import org.home.mvc.model.BattleModel
import tornadofx.Component
import tornadofx.ScopedInstance

abstract class GameBean: Component(), ScopedInstance

abstract class GameComponent: GameBean() {
    protected val modelView: BattleModel by GameScope.inject()
    protected val applicationProperties = modelView.applicationProperties
    protected open val currentPlayer = modelView.currentPlayer
}

abstract class GameController : GameComponent()
