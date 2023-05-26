package org.home.app.di

import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope

object GameScope {

    class NewGameScope : KoinScopeComponent {
        override val scope: KScope by lazy { createScope(this) }
        fun close() = scope.close()
    }

    fun createNew() {
        FxScopes.newGame()
        KoinScopes.newGame()
    }

    fun get(): KScope = KoinScopes.getGameScope()
}

