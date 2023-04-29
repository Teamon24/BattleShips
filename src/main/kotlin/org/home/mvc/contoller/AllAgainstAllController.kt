package org.home.mvc.contoller

import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetSettingsReceived
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.net.PlayerSocket
import org.home.net.action.Action
import org.home.net.action.ActionExtensions.connectedPlayersExcept
import org.home.net.action.ActionExtensions.fleetSettings
import org.home.net.action.ActionExtensions.fleetsReadinessExcept
import org.home.net.action.ActionExtensions.readyPlayers
import org.home.net.action.BattleEndAction
import org.home.net.action.ConnectionAction
import org.home.net.action.ConnectionsAction
import org.home.net.action.DefeatAction
import org.home.net.action.DisconnectAction
import org.home.net.action.EmptyAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.HitAction
import org.home.net.action.MissAction
import org.home.net.action.PlayerReadinessAction
import org.home.net.action.ShotAction
import org.home.net.action.TurnAction
import org.home.utils.PlayersSockets
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.excludeFrom
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.removeFrom
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
            fire(ConnectedPlayerReceived(it))

            val connectedSocket = sockets[it.player]

            connectedSocket {
                send {
                    fleetSettings(model)
                    connectedPlayersExcept(player!!, model)
                    fleetsReadinessExcept(player!!, model)
                    readyPlayers(model)
                }
                excludeFrom(sockets).send(it)
            }

        }
    }

    override fun onMessage(action: Action) {
        log { action }
    }

    override fun onTurn(action: TurnAction) {
        TODO("onTurn")
    }

    override fun onReady(action: PlayerReadinessAction) {
        model.playersReadiness[action.player] = action.isReady
        if (action.isReady) {
            fire(PlayerIsReadyReceived(action.player))
        } else {
            fire(PlayerIsNotReadyReceived(action.player))
        }
    }

    override fun onFleetSettings(action: FleetSettingsAction) {
        fire(FleetSettingsReceived(action))
    }

    override fun onPlayers(action: ConnectionsAction) {
        fire(ConnectedPlayersReceived(action.players))
    }
}





