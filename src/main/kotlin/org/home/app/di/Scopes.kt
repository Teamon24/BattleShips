package org.home.app.di

import home.extensions.AnysExtensions.name
import org.home.app.di.GameScope.NewGameScope
import org.home.mvc.model.BattleViewModelImpl
import org.home.utils.componentName
import org.home.utils.log
import org.home.utils.logInject
import org.koin.java.KoinJavaComponent.getKoin
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.View
import kotlin.reflect.KClass

typealias KScope = org.koin.core.scope.Scope
typealias FxScope = tornadofx.Scope

sealed class Scopes<ScopeType> {
    protected var _gameScope: ScopeType? = null

    open fun getGameScope() = _gameScope ?: run {
        _gameScope = getScope()
        _gameScope!!
    }

    abstract fun getScope(): ScopeType
    abstract fun createNew()
}

object KoinScopes: Scopes<KScope>() {
    override fun getScope() = getKoin().createScope<NewGameScope>()

    override fun createNew() {
        _gameScope?.close()
        _gameScope = getScope()
        log { "$name created NEW - ${getGameScope().name}" }
    }
}


object FxScopes: Scopes<FxScope>() {
    override fun getScope() = FxScope()

    override fun createNew() {
        _gameScope = getScope()
        BattleViewModelImpl().inScope(getGameScope())
        log { "$name created NEW - ${getGameScope().name}" }
    }

    private inline fun <reified T> T.inScope(scope: FxScope) where T : Component, T : ScopedInstance {
        tornadofx.setInScope(this, scope)
        log { "$componentName with ${scope.name}" }
    }
}
