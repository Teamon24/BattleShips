package org.home.mvc.contoller

import org.home.net.PlayerSocket
import org.home.net.action.Action
import org.home.net.action.BattleEndAction
import org.home.net.action.PlayersConnectionsAction
import org.home.net.action.PlayerConnectionAction
import org.home.net.action.DefeatAction
import org.home.net.action.DisconnectAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.HitAction
import org.home.net.action.MissAction
import org.home.net.action.PlayerReadinessAction
import org.home.net.action.ShotAction
import org.home.net.action.TurnAction
import org.home.utils.PlayersSockets
import org.home.utils.extensions.className
import kotlin.collections.Collection


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
        playerConnectionAction: PlayerConnectionAction,
    ) {
        TODO("onConnect")
    }

    override fun onMessage(action: Action) {
        TODO("onTurn")
    }

    override fun onTurn(action: TurnAction) {
        TODO("onTurn")
    }

    override fun onReady(action: PlayerReadinessAction) {
        TODO("onReady")
    }

    override fun onFleetSettings(action: FleetSettingsAction) {
        TODO("${this.className}#onFleetSettings")
    }

    override fun onPlayers(action: PlayersConnectionsAction) {
        TODO("${this.className}#onPlayers")
    }
}





