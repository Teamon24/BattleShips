package org.home.mvc.view

import org.home.mvc.di.Injector
import org.home.mvc.model.BattleModel
import org.home.utils.componentName
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.log
import tornadofx.Component
import tornadofx.Scope
import tornadofx.ScopedInstance
import tornadofx.View

abstract class AbstractGameView(title: String): View(title = title) {

    inline fun <reified T> newGame()
            where T : Component,
                  T : ScopedInstance = Injector.newGame<T>(this@AbstractGameView)

    internal val model: BattleModel by newGame()
    internal val applicationProperties = model.applicationProperties
    internal val currentPlayer = applicationProperties.currentPlayer
}

inline fun <reified T> T.withScope(scope: Scope): T where T : Component, T : ScopedInstance {
    tornadofx.setInScope(this, scope)
    log { "${this.componentName} with ${scope.name}" }
    return this
}

inline fun <reified T> T.inScope(scope: Scope) where T : Component, T : ScopedInstance {
    tornadofx.setInScope(this, scope)
    log { "${this.componentName} with ${scope.name}" }
}