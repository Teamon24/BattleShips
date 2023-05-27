package org.home.app.di

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.app.ApplicationProperties
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.FleetReadinessLabelController
import org.home.mvc.contoller.FleetReadinessPaneController
import org.home.mvc.contoller.ShipsPane.Type.NO_SHIPS_NUMBERS
import org.home.mvc.contoller.ShipsPane.Type.WITH_SHIPS_NUMBERS
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.contoller.ShipsTypesPaneComponent
import org.home.mvc.contoller.ShipsTypesPaneController
import org.home.mvc.contoller.ShotNotifierStrategies
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
import org.home.mvc.view.component.ViewSwitch.Type.OPEN
import org.home.mvc.view.component.ViewSwitch.Type.REPLACEMENT
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

fun gameModule(properties: String): Module {
    return module {
        single { ApplicationProperties(properties) }

        single { ViewReplacement }
        single { ViewOpenWindow }

        factory {
            when (appProps().viewSwitchType) {
                REPLACEMENT -> get<ViewReplacement>()
                OPEN -> get<ViewOpenWindow>()
            }
        }

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

        factory<BattleController<Action>> {
            isServer(get<BattleServer>(), get<BattleClient>())
        }

        scope<GameScope.NewGameScope> {
            scoped<BattleViewModel> { BattleViewModelImpl() }

            scoped { FleetGridController() }
            scoped { FleetGridCreator() }
            scoped { BattleEndingComponent() }
            scoped { BattleStartButtonController() }
            scoped { BattleViewExitButtonController() }

            factory {
                isServer(BattleStartButtonComponentForServer(), BattleStartButtonComponentForClient())
            }

            scoped { SettingsPaneController() }
            scoped { SettingsFieldsController() }
            scoped { SubscriptionComponent() }

            scoped { ShipsTypesController() }
            scoped { ShipsTypesPaneController() }
            scoped { FleetReadinessPaneController() }
            factory { FleetReadinessLabelController() }

            scoped { ShipsTypesPaneComponent() }
            scoped { FleetGridHandlers() }
            scoped { EnemiesViewController() }
            scoped { EnemiesListViewController() }
            scoped { CurrentFleetController() }

            scoped { ViewSwitchButtonController }

            scoped { FleetReadinessPaneController() }
            scoped { ShipsTypesPaneController() }

            factory {
                when (appProps().shipsPane) {
                    NO_SHIPS_NUMBERS -> get<FleetReadinessPaneController>()
                    WITH_SHIPS_NUMBERS -> get<ShipsTypesPaneController>()
                }
            }
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
                isServer(NewServerViewControllerForServer(), NewServerViewControllerForClient())
            }
        }
    }
}


fun <T> Scope.isServer(forServer: T, forClient: T): T =
    appProps().isServer.then { forServer }.or { forClient }

private fun Scope.appProps() = get<ApplicationProperties>()
