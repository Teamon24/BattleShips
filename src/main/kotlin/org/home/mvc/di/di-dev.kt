package org.home.mvc.di

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.ShotNotifierStrategies.di
import org.home.mvc.view.Scopes
import org.home.net.BattleClient
import org.home.net.PlayerSocket
import org.home.net.message.Action
import org.home.net.server.BattleServer
import org.home.net.server.ConnectionsListener
import org.home.net.server.MessageProcessor
import org.home.net.server.MessageReceiver
import org.home.net.server.MultiServer
import org.home.utils.componentName
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.logInject
import org.koin.dsl.module
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.find
import kotlin.properties.ReadOnlyProperty

object Injector  {

    inline fun <reified T> newGame(c: Component):
            ReadOnlyProperty<Component, T>
            where T : Component,
                  T : ScopedInstance = ReadOnlyProperty { _, _ ->

        val scope = Scopes.gameScope
        find<T>(scope).apply {
            logInject(c.componentName, this, scope)
        }
    }

    inline fun <reified T> netDi(c: Component):
            ReadOnlyProperty<Component, T>
            where T : Component,
                  T : ScopedInstance = ReadOnlyProperty { component, kProperty ->

        val value = di<T>().getValue(component, kProperty)
        logInject(c, value)
        value
    }
}

val diDev = { props: String, player: Int, players: Int ->
    module {
        single { ApplicationProperties(props, player, players) }

        single { ShotNotifierStrategies }
        single { ConnectionsListener<Action, PlayerSocket>() }
        single { MessageReceiver<Action, PlayerSocket>() }
        single { MessageProcessor<Action, PlayerSocket>() }

        single { BattleServer() }
        single { BattleClient() }

        single<MultiServer<Action, PlayerSocket>> { get<BattleServer>() }

        factory<BattleController> {
            val isServer = get<ApplicationProperties>().isServer
            isServer then get<BattleServer>() or get<BattleClient>()
        }
    }
}
