package org.home.app.di

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.app.ApplicationProperties
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.contoller.ShipsTypesPaneComponent
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.server.AddressComponentImpl
import org.home.mvc.contoller.server.AddressComponentImplDebug
import org.home.mvc.contoller.server.BattleClient
import org.home.mvc.contoller.server.BattleEndingComponent
import org.home.mvc.contoller.server.BattleServer
import org.home.mvc.contoller.server.PlayerSocket
import org.home.mvc.contoller.server.PlayerTurnComponent
import org.home.mvc.contoller.server.ShotProcessingComponent
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.contoller.serverTransfer.NewServerViewControllerForClient
import org.home.mvc.contoller.serverTransfer.NewServerViewControllerForServer
import org.home.mvc.model.BattleViewModel
import org.home.mvc.model.BattleViewModelImpl
import org.home.mvc.view.battle.CurrentFleetController
import org.home.mvc.view.battle.EnemiesListViewController
import org.home.mvc.view.battle.EnemiesViewController
import org.home.mvc.view.battle.SettingsFieldsController
import org.home.mvc.view.battle.SettingsPaneController
import org.home.mvc.view.battle.subscription.SubscriptionComponent
import org.home.mvc.view.component.ViewOpenWindow
import org.home.mvc.view.component.ViewReplacement
import org.home.mvc.view.component.ViewSwitch.ViewSwitchType.OPEN
import org.home.mvc.view.component.ViewSwitch.ViewSwitchType.REPLACEMENT
import org.home.mvc.view.component.button.BattleStartButtonComponentForClient
import org.home.mvc.view.component.button.BattleStartButtonComponentForServer
import org.home.mvc.view.component.button.BattleStartButtonController
import org.home.mvc.view.component.button.BattleViewExitButtonController
import org.home.mvc.view.component.button.ViewSwitchButtonController
import org.home.mvc.view.fleet.FleetGridController
import org.home.mvc.view.fleet.FleetGridCreator
import org.home.mvc.view.fleet.FleetGridHandlers
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CLASS
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CSS
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TIMELINE
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.TRANSITION
import org.home.mvc.view.fleet.style.FleetGridStyleCssChange
import org.home.mvc.view.fleet.style.FleetGridStyleTimeline
import org.home.mvc.view.fleet.style.FleetGridStyleTransition
import org.home.net.server.ConnectionsListener
import org.home.net.server.MessageProcessor
import org.home.net.server.MessageReceiver
import org.home.net.server.MultiServer
import org.home.net.server.MultiServer.MultiServerSockets
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun netControllers(properties: String): Module {
    return module {
        single { ApplicationProperties(properties) }

        single { ShotNotifierStrategies }
        single { ConnectionsListener<Action, PlayerSocket>() }
        single { MessageReceiver<Action, PlayerSocket>() }
        single { MessageProcessor<Action, PlayerSocket>() }
        single { MultiServerSockets<PlayerSocket>() }

        single { BattleServer() }
        single { BattleClient() }
        single { ShotProcessingComponent() }
        single { PlayerTurnComponent() }

        single<MultiServer<Action, PlayerSocket>> { get<BattleServer>() }

        single { ViewReplacement }
        single { ViewOpenWindow }
        single { ViewSwitchButtonController }

        factory {
            when (get<ApplicationProperties>().viewSwitchType) {
                REPLACEMENT -> get<ViewReplacement>()
                OPEN -> get<ViewOpenWindow>()
            }
        }

        factory<BattleController<Action>> {
            val isServer = get<ApplicationProperties>().isServer
            isServer then get<BattleServer>() or get<BattleClient>()
        }
    }
}

fun gameScoped(): Module {
    return module {
        scope<GameScope.NewGameScope> {
            scoped<BattleViewModel> { BattleViewModelImpl() }

            scoped { FleetGridController() }
            scoped { FleetGridCreator() }
            scoped { BattleEndingComponent() }
            scoped { BattleStartButtonController() }
            scoped { BattleViewExitButtonController() }
            scoped {
                get<ApplicationProperties>().isDebug
                    .then { AddressComponentImplDebug() }
                    .or { AddressComponentImpl() }
            }

            factory {
                get<ApplicationProperties>().isServer
                    .then { BattleStartButtonComponentForServer() }
                    .or   { BattleStartButtonComponentForClient() }
            }

            scoped { SettingsPaneController() }
            scoped { SettingsFieldsController() }
            scoped { SubscriptionComponent() }

            scoped { ShipsTypesController() }
            scoped { ShipsTypesPaneController() }
            scoped { ShipsTypesPaneComponent() }
            scoped { FleetGridHandlers() }
            scoped { EnemiesViewController() }
            scoped { EnemiesListViewController() }
            scoped { CurrentFleetController() }

            scoped { AwaitConditions() }

            factory { (type: FleetGreedStyleUpdate) ->
                when (type) {
                    CLASS -> FleetGridStyleAddClass
                    TRANSITION -> FleetGridStyleTransition
                    CSS -> FleetGridStyleCssChange
                    TIMELINE -> FleetGridStyleTimeline
                }
            }

            factory {
                get<ApplicationProperties>().isServer
                    .then { NewServerViewControllerForServer() }
                    .or   { NewServerViewControllerForClient() }
            }
        }
    }
}

fun <T> Scope.isServer(forServer: T, forClient: T): T =
    get<ApplicationProperties>().isServer.then { forServer }.or { forClient }
