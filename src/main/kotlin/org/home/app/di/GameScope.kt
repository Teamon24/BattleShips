package org.home.app.di

import home.extensions.AnysExtensions.name
import org.home.utils.log
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.find
import kotlin.properties.ReadOnlyProperty

object GameScope {

    class NewGameScope : KoinScopeComponent {
        override val scope: KScope by lazy { createScope(this) }
        fun close() = scope.close()
    }

    enum class GameInject { KOIN, FX }
    val gameInject = GameInject.FX

    private val newGameInitializer = scopes()

    private fun scopes() = when (gameInject) {
            GameInject.KOIN -> KoinScopes
            GameInject.FX -> FxScopes
        }

    fun createNew() {
        newGameInitializer.createNew()
        log { "$gameInject new game created" }
    }

    inline fun <reified T> inject(): ReadOnlyProperty<Component, T> where
            T : Component,
            T : ScopedInstance = ReadOnlyProperty { _, _ ->

        when (gameInject) {
            GameInject.KOIN -> KoinScopes.getGameScope().get<T>()
            GameInject.FX -> find<T>(FxScopes.getGameScope())
        }.apply {
            log { "bean - ${this.name}" }
        }
    }
}