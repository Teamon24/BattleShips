package org.home.app.di

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.net.BattleClient
import org.home.net.PlayerSocket
import org.home.net.message.Action
import org.home.net.server.BattleEventEmitter
import org.home.net.server.BattleServer
import org.home.net.server.ConnectionsListener
import org.home.net.server.MessageProcessor
import org.home.net.server.MessageReceiver
import org.home.net.server.MultiServer
import org.koin.dsl.module
import home.extensions.BooleansExtensions.then
import home.extensions.BooleansExtensions.or
import org.home.net.server.ShotProcessingComponent
import org.home.net.server.PlayerTurnComponent

val diDev = { props: String, player: Int, players: Int ->
    module {
        single { ApplicationProperties(props, player, players) }

        single { ShotNotifierStrategies }
        single { BattleEventEmitter }
        single { ConnectionsListener<Action, PlayerSocket>() }
        single { MessageReceiver<Action, PlayerSocket>() }
        single { MessageProcessor<Action, PlayerSocket>() }
        single { MultiServer.MultiServerSockets<PlayerSocket>() }

        single { BattleServer() }
        single { BattleClient() }
        single { ShotProcessingComponent() }
        single { PlayerTurnComponent() }

        single<MultiServer<Action, PlayerSocket>> { get<BattleServer>() }

        factory<BattleController<Action>> {
            val isServer = get<ApplicationProperties>().isServer
            isServer then get<BattleServer>() or get<BattleClient>()
        }
    }
}
