package org.home.mvc.view

import tornadofx.Scope

object Scopes {
    private var gameScopeBackingField: Scope? = null

    val gameScope: Scope get() = gameScopeBackingField ?: run {
        gameScopeBackingField = Scope()
        gameScopeBackingField!!
    }

    fun newGameScope() { gameScopeBackingField = Scope() }
}