package org.home.app.di

import home.extensions.BooleansExtensions.so
import org.home.app.di.GameScope.GameInject.FX
import org.home.app.di.GameScope.GameInject.KOIN
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass
import org.home.mvc.view.fleet.style.FleetGridStyleComponent
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CLASS
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CSS
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TIMELINE
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TRANSITION
import org.home.mvc.view.fleet.style.FleetGridStyleCssChange
import org.home.mvc.view.fleet.style.FleetGridStyleTimeline
import org.home.mvc.view.fleet.style.FleetGridStyleTransition
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.parameter.parametersOf
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.find
import kotlin.properties.ReadOnlyProperty

object GameScope {

    enum class GameInject {
        KOIN, FX;

        companion object {
            inline fun GameInject.isKoin(onTrue: () -> Unit) = (this == KOIN).so(onTrue)
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

    inline fun <reified T : FleetGridStyleComponent> fleetGridStyle():
            ReadOnlyProperty<Component, FleetGridStyleComponent> =

        ReadOnlyProperty { _, _ ->
            when (gameInject) {
                KOIN -> KoinScopes.getGameScope().get() {
                    parametersOf(getType<T>())
                }
                FX -> find(FxScopes.getGameScope())
            }
        }

    inline fun <reified T : FleetGridStyleComponent> getType() = when {
        T::class == FleetGridStyleAddClass::class -> CLASS
        T::class == FleetGridStyleCssChange::class -> CSS
        T::class == FleetGridStyleTransition::class -> TRANSITION
        T::class == FleetGridStyleTimeline::class -> TIMELINE
        else -> throw RuntimeException("no when-branch for ${T::class}")
    }
}