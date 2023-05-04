package org.home.mvc.di

import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.AllAgainstAllController
import org.home.mvc.contoller.Conditions
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.contoller.ShipTypePaneComponent
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.fleet.FleetGridCreator
import org.home.net.BattleClient
import org.home.net.PlayerSocket
import org.home.net.action.Action
import org.home.net.server.BattleServer
import org.home.net.server.ConnectionsListener
import org.home.net.server.MessageProcessor
import org.home.net.server.MessageReceiver
import org.home.net.server.MultiServer
import org.koin.dsl.module


val diDev = { props: String, player: Int, players: Int ->
    module {
        single { ApplicationProperties(props, player, players) }

        single { BattleModel() }
        single { BattleCreationView() }

        single { FleetGridController() }
        single { FleetGridCreator() }

        single { ShipsTypesPaneController() }
        single { ShipTypePaneComponent() }
        single { ShipsTypesController() }
        single { Conditions() }

        factory<GameTypeController> { AllAgainstAllController() }

        single { BattleServer(get(), get(), get(), get(), get(), get())  }
        single { BattleClient(get()) }

        single { ShotNotifierStrategies }

        single {
            if (get<ApplicationProperties>().isServer) {
                get<BattleServer>()
            } else {
                get<BattleClient>()
            }
        }

        single<MultiServer<Action, PlayerSocket>> { get<BattleServer>() }



        factory { BattleView() }
        single { ConnectionsListener<Action, PlayerSocket>() }
        single { MessageReceiver<Action, PlayerSocket>() }
        single { MessageProcessor<Action, PlayerSocket>() }
    }
}
