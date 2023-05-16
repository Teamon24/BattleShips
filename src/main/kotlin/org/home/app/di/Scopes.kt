package org.home.app.di

import home.extensions.AnysExtensions.name
import org.home.mvc.model.BattleModel
import org.home.utils.componentName
import org.home.utils.log
import org.koin.java.KoinJavaComponent
import tornadofx.Component
import tornadofx.ScopedInstance

typealias KScope = org.koin.core.scope.Scope
typealias FxScope = tornadofx.Scope

sealed interface Scopes {
    fun createNew()
}

object KoinScopes: Scopes {
    private var gameScopeBackingField: KScope? = null
    val gameScope
        get() = gameScopeBackingField ?: run {
            gameScopeBackingField = KoinJavaComponent.getKoin().createScope<GameScope.NewGameScope>()
            gameScopeBackingField!!
        }

    override fun createNew() {
        gameScopeBackingField?.close()
        gameScopeBackingField = KoinJavaComponent.getKoin().createScope<GameScope.NewGameScope>()
    }
}


object FxScopes: Scopes {
    private var gameScopeBackingField: FxScope? = null

    val gameScope: FxScope get() = gameScopeBackingField ?: run {
        gameScopeBackingField = FxScope()
        gameScopeBackingField!!
    }

    override fun createNew() {
        gameScopeBackingField = FxScope()
        BattleModel().inScope(gameScope)
    }

    private inline fun <reified T> T.inScope(scope: FxScope) where T : Component, T : ScopedInstance {
        tornadofx.setInScope(this, scope)
        log { "$componentName with ${scope.name}" }
    }
}
