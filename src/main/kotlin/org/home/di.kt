package org.home

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
import org.home.mvc.view.fleet.FleetGridCreationController
import org.home.mvc.view.fleet.FleetGridCreationView
import org.home.mvc.view.fleet.FleetGridCreator
import org.home.net.BattleClient
import org.home.net.server.BattleServer
import org.koin.dsl.module


val diModule = { props: String, player: Int, players: Int ->
    module {
        single { ApplicationProperties(props, player, players) }

        single { BattleModel() }
        single { BattleCreationView() }

        single { ShipsTypesPaneController() }
        single { ShipTypePaneComponent() }
        single { ShipsTypesController() }
        single { Conditions() }

        factory<GameTypeController> { AllAgainstAllController() }

        single { BattleServer() }
        single { BattleClient() }

        single { ShotNotifierStrategies }

        single {
            if (get<ApplicationProperties>().isServer) {
                get<BattleServer>()
            } else {
                get<BattleClient>()
            }
        }

        factory { FleetGridCreationView() }
        factory { FleetGridCreationController() }

        factory { BattleView() }
    }
}
