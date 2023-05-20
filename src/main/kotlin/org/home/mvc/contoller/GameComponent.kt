package org.home.mvc.contoller

import org.home.app.ApplicationProperties
import org.home.app.di.GameScope
import org.home.mvc.model.BattleViewModel
import tornadofx.Component
import tornadofx.ScopedInstance

abstract class GameBean: Component(), ScopedInstance

abstract class GameComponent: GameBean() {
    protected val modelView by GameScope.inject<BattleViewModel>()
    protected val applicationProperties by di<ApplicationProperties>()
    protected open val currentPlayer = modelView.getCurrentPlayer()
}

abstract class GameController : GameComponent()
