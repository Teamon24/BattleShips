package org.home.app.di

import home.extensions.AnysExtensions.name
import org.home.app.di.GameScope.NewGameScope
import org.home.mvc.model.BattleModel
import org.home.utils.componentName
import org.home.utils.log
import org.koin.java.KoinJavaComponent.getKoin
import tornadofx.Component
import tornadofx.ScopedInstance

typealias KScope = org.koin.core.scope.Scope
typealias FxScope = tornadofx.Scope

sealed class Scopes<ScopeType> {
    protected var gameScopeBackingField: ScopeType? = null

    open fun getGameScope(): ScopeType = gameScopeBackingField ?: run {
        gameScopeBackingField = getScope()
        gameScopeBackingField!!
    }

    abstract fun getScope(): ScopeType
    abstract fun createNew()
}

object KoinScopes: Scopes<KScope>() {
    override fun getScope() = getKoin().createScope<NewGameScope>()

    override fun createNew() {
        gameScopeBackingField?.close()
        gameScopeBackingField = getKoin().createScope<NewGameScope>()
    }
}


object FxScopes: Scopes<FxScope>() {

    override fun getScope() = FxScope()

    override fun createNew() {
        gameScopeBackingField = FxScope()
        BattleModel().inScope(getGameScope())
    }

    private inline fun <reified T> T.inScope(scope: FxScope) where T : Component, T : ScopedInstance {
        tornadofx.setInScope(this, scope)
        log { "$componentName with ${scope.name}" }
    }
}
