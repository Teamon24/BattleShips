package org.home.app.di

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.contoller.ShipsTypesPaneComponent
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.server.BattleClient
import org.home.mvc.contoller.server.BattleServer
import org.home.mvc.contoller.server.PlayerSocket
import org.home.mvc.contoller.server.PlayerTurnComponent
import org.home.mvc.contoller.server.ShotProcessingComponent
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.model.BattleModel
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.fleet.FleetGridCreator
import org.home.net.server.ConnectionsListener
import org.home.net.server.MessageProcessor
import org.home.net.server.MessageReceiver
import org.home.net.server.MultiServer
import org.koin.core.module.Module
import org.koin.dsl.module

fun singletons(properties: String): Module {
    return module {
        single { ApplicationProperties(properties) }

        single { ShotNotifierStrategies }
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

fun gameScoped(): Module {
    return module {
        scope<GameScope.NewGameScope> {
            scoped { BattleModel() }
            scoped { FleetGridController() }
            scoped { FleetGridCreator() }

            scoped { ShipsTypesController() }
            scoped { ShipsTypesPaneController() }
            scoped { ShipsTypesPaneComponent() }

            scoped { AwaitConditions() }
        }
    }
}
