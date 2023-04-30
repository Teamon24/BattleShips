package org.home.mvc.contoller

import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.DSLContainer.Companion.eventbus
import org.home.mvc.contoller.events.FleetSettingsReceived
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.net.PlayerSocket
import org.home.net.action.Action
import org.home.net.action.AreReadyAction
import org.home.net.action.BattleEndAction
import org.home.net.action.ConnectedPlayersAction
import org.home.net.action.ConnectionAction
import org.home.net.action.DefeatAction
import org.home.net.action.DisconnectAction
import org.home.net.action.EmptyAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.FleetsReadinessAction
import org.home.net.action.HitAction
import org.home.net.action.MissAction
import org.home.net.action.PlayerReadinessAction
import org.home.net.action.ShotAction
import org.home.net.action.TurnAction
import org.home.utils.PlayersSockets
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.excludeFrom
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.extensions.className
import org.home.utils.log


class AllAgainstAllController : GameTypeController() {
    override fun onShot(sockets: Collection<PlayerSocket>, action: ShotAction) {
        TODO("onShot")
    }

    override fun onHit(sockets: Collection<PlayerSocket>, action: HitAction) {
        TODO("onHit")
    }

    override fun onMiss(sockets: Collection<PlayerSocket>, action: MissAction) {
        TODO("onMiss")
    }

    override fun onEmpty(action: EmptyAction) {
        TODO("onEmpty")
    }

    override fun onDefeat(action: DefeatAction) {
        TODO("onDefeat")
    }

    override fun onDisconnect(action: DisconnectAction) {
        TODO("onDisconnect")
    }

    override fun onEndGame(action: BattleEndAction) {
        TODO("onEndGame")
    }

    override fun onConnect(
        sockets: PlayersSockets,
        connectionAction: ConnectionAction,
    ) {
        connectionAction.also {
            eventbus {
                +ConnectedPlayerReceived(it)
            }

            val connectedSocket = sockets[it.player]

            connectedSocket {
                model.also {
                    send {
                        +FleetSettingsAction(it)
                        +ConnectedPlayersAction(it.playersNames.exclude(player!!))
                        +fleetsReadinessExcept(player!!, it)
                        +AreReadyAction(it.playersReadiness.thoseAreReady)
                    }
                }

                excludeFrom(sockets).send(it)
            }
        }
    }

    private fun fleetsReadinessExcept(
        connectedPlayer: String,
        model: BattleModel,
    ): FleetsReadinessAction {
        val states = model.fleetsReadiness
            .exclude(connectedPlayer)
            .map { (player, state) ->
                player to state.map { (shipType, number) -> shipType to number.value }.toMap()
            }
            .toMap()

        return FleetsReadinessAction(states)
    }

    override fun onMessage(action: Action) {
        TODO("onTurn")
    }

    override fun onTurn(action: TurnAction) {
        TODO("onTurn")
    }

    override fun onReady(action: PlayerReadinessAction) {
        action {
            model.playersReadiness[player] = isReady
            eventbus {
                if (isReady) {
                    + PlayerIsReadyReceived(player)
                } else {
                    + PlayerIsNotReadyReceived(player)
                }
            }
        }
    }

    override fun onFleetSettings(action: FleetSettingsAction) {
        fire(FleetSettingsReceived(action))
    }

    override fun onPlayers(action: ConnectedPlayersAction) {
        TODO("${this.className}#onPlayers")
    }
}





