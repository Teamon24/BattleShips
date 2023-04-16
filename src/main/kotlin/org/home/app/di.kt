package org.home.app

import org.home.mvc.contoller.AllAgainstAllController
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.PlayersNamesPaneController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.net.BattleClient
import org.home.net.BattleServer
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.ShipTypePaneComponent
import org.home.mvc.view.fleet.FleetCreationView
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import tornadofx.Component
import tornadofx.FX
import tornadofx.getInstance
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


val diModule = { props: String ->
    module {
        single { ApplicationProperties(props) }

        singleOf(::BattleModel)
        singleOf(::BattleCreationView)
        singleOf(::PlayersNamesPaneController)

        singleOf(::ShipsTypesPaneController)
        singleOf(::ShipTypePaneComponent)


        factory<GameTypeController> { AllAgainstAllController() }

        factory { (isServer: Boolean) -> battleController(isServer) }


        factory { FleetCreationView() }
        factory { BattleView() }

    }
}

private fun Scope.battleController(isServer: Boolean) = when {
    isServer -> BattleClient(get())
    else -> BattleServer(get())
}

/**
 * Fix for method [Component.di].
 * The reason of fix: [Component.di] caches bean from di container. It should not happen in case when bean scope is prototype.
 */
inline fun <reified T : Any> injecting(): ReadOnlyProperty<Component, T> =
    object : ReadOnlyProperty<Component, T> {
        override fun getValue(thisRef: Component, property: KProperty<*>): T {
            val diContainer = FX.dicontainer ?: throw notConfiguredInjectorError()
            return diContainer.getInstance()
        }

        fun notConfiguredInjectorError() = AssertionError(
            "Injector is not configured, so bean of type ${T::class} cannot be resolved"
        )
    }