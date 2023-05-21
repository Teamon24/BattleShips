package org.home.app.di

import org.home.utils.logInject
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.emptyParametersHolder
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent
import tornadofx.Component
import tornadofx.View
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

val koin = KoinJavaComponent.getKoin()

typealias Injection<T> = ReadOnlyProperty<Component, T>

object ViewInjector {
    fun <T: View> Component.getView(to: KClass<T>, scope: FxScope? = null): T {
        val found = scope?.let { tornadofx.find(to, it) } ?: find(to)

        scope ?: logInject(found, disabled = true)
        scope?.let {  logInject(found, it, disabled = true) }

        return found
    }
}


inline fun <reified T> Component.gameScope(parameter: Any? = null): Injection<T> = injection { byGameScope(parameter) }
inline fun <reified T : Any> Component.gameScope(): Injection<T> = injection { byGameScope<T, Any?>() }

inline fun <reified T: Any> Component.noScope(parameter: Any? = null): Injection<T> =
    injection {
        koin.get<T>(parameters = getParams(parameter))
            .also {
                logInject(it, disabled = true)
            }
    }

inline fun <reified T : Any, reified P> Component.byGameScope(parameter: P? = null): T {
    val gameScope = KoinScopes.getGameScope()
    return gameScope
        .get<T>(parameters = getParams<P>(parameter))
        .also {
            logInject(it, gameScope, disabled = true)
        }
}


inline fun <reified P> getParams(parameter: P?) = if (parameter != null) definitionOf<P>(parameter) else empty

inline fun <V> injection(crossinline body: () -> V): ReadOnlyProperty<Any, V> = ReadOnlyProperty { _, _ -> body() }

inline fun <reified P> definitionOf(parameter: P?): ParametersDefinition = { parametersOf(parameter) }

val empty: ParametersDefinition = { emptyParametersHolder() }

