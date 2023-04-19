package org.home

import org.home.mvc.contoller.AllAgainstAllController
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.PlayersNamesPaneController
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.model.BattleModel
import org.home.mvc.view.battle.BattleCreationView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.ShipTypePaneComponent
import org.home.mvc.view.fleet.FleetGridCreationComponent
import org.home.mvc.view.fleet.FleetGridCreationView
import org.home.net.BattleClient
import org.home.net.BattleServer
import org.koin.dsl.module


val diModule = { props: String, player: Int, players: Int ->
    module {
        single { ApplicationProperties(props, player, players) }

        single { BattleModel() }
        single { BattleCreationView() }
        single { PlayersNamesPaneController() }
        single { ShipsTypesPaneController() }
        single { ShipTypePaneComponent() }
        single { ShipsTypesController() }

        factory<GameTypeController> { AllAgainstAllController() }

        single { BattleServer() }
        single { BattleClient() }
        single {
            if (get<ApplicationProperties>().isServer) {
                get<BattleServer>()
            } else {
                get<BattleClient>()
            }
        }

        factory { FleetGridCreationView() }
        factory { FleetGridCreationComponent() }

        factory { BattleView() }
    }
}
