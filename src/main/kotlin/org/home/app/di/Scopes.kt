package org.home.app.di

import home.extensions.AnysExtensions.name
import org.home.utils.componentName
import org.home.utils.log
import tornadofx.Component
import tornadofx.Scope
import tornadofx.ScopedInstance

object Scopes {
    private var gameScopeBackingField: Scope? = null

    val gameScope: Scope get() = gameScopeBackingField ?: run {
        gameScopeBackingField = Scope()
        gameScopeBackingField!!
    }

    fun newGameScope() { gameScopeBackingField = Scope() }

    inline fun <reified T> T.inScope(scope: Scope) where T : Component, T : ScopedInstance {
        tornadofx.setInScope(this, scope)
        log { "$componentName with ${scope.name}" }
    }
}