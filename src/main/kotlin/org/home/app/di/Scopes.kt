package org.home.app.di

import home.extensions.AnysExtensions.name
import org.home.app.di.GameScope.NewGameScope
import org.home.mvc.model.BattleViewModelImpl
import org.home.utils.componentName
import org.home.utils.log
import org.koin.java.KoinJavaComponent.getKoin
import tornadofx.Component
import tornadofx.ScopedInstance

typealias KScope = org.koin.core.scope.Scope
typealias FxScope = tornadofx.Scope

sealed class Scopes<ScopeType> {
    protected var _gameScope: ScopeType? = null

    open fun getGameScope() = _gameScope ?: run {
        _gameScope = createScope()
        _gameScope!!
    }

    protected abstract fun createScope(): ScopeType
    abstract fun newGame()
}

object KoinScopes: Scopes<KScope>() {
    override fun createScope() = getKoin().createScope<NewGameScope>()

    override fun newGame() {
        _gameScope?.close()
        _gameScope = createScope()
        log { "$name created NEW - ${getGameScope().name}" }
    }
}


object FxScopes: Scopes<FxScope>() {
    override fun createScope() = FxScope()

    override fun newGame() {
        _gameScope = createScope()
        BattleViewModelImpl().inScope(getGameScope())
        log { "$name created NEW - ${getGameScope().name}" }
    }

    private inline fun <reified T> T.inScope(scope: FxScope) where T : Component, T : ScopedInstance {
        tornadofx.setInScope(this, scope)
        log { "$componentName with ${scope.name}" }
    }
}
