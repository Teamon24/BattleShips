package org.home.app.di

import home.extensions.BooleansExtensions.so
import org.home.app.di.GameScope.GameInject.FX
import org.home.app.di.GameScope.GameInject.KOIN
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.find
import kotlin.properties.ReadOnlyProperty

object GameScope {

    enum class GameInject {
        KOIN, FX;

        companion object {
            inline fun GameInject.isKoin(onTrue: () -> Unit) = (this == FX).so(onTrue)
        }
    }

    val gameInject = KOIN

    class NewGameScope : KoinScopeComponent {
        override val scope: KScope by lazy { createScope(this) }
        fun close() = scope.close()
    }

    fun createNew() {
        when (gameInject) {
            KOIN -> {
                FxScopes.createNew()
                KoinScopes.createNew()
            }
            FX -> FxScopes.createNew()
        }

    }

    inline fun <reified T> inject(): ReadOnlyProperty<Component, T> where
            T : Component,
            T : ScopedInstance = ReadOnlyProperty { _, _ ->

        when (gameInject) {
            KOIN -> KoinScopes.getGameScope().get<T>()
            FX -> find<T>(FxScopes.getGameScope())
        }
    }
}