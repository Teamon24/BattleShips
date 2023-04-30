package org.home.mvc.contoller

import org.home.mvc.ApplicationProperties
import org.home.mvc.model.BattleModel
import org.home.net.action.ConnectionAction
import org.home.net.action.DefeatAction
import org.home.net.action.DisconnectAction
import org.home.net.action.EmptyAction
import org.home.net.action.BattleEndAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.HitAction
import org.home.net.action.Action
import org.home.net.action.MissAction
import org.home.net.PlayerSocket
import org.home.net.action.PlayerReadinessAction
import org.home.net.action.ConnectedPlayersAction
import org.home.net.action.ShotAction
import org.home.net.action.TurnAction
import org.home.utils.PlayersSockets
import tornadofx.Controller

abstract class GameTypeController: Controller() {
    protected val applicationProperties: ApplicationProperties by di()
    protected val model: BattleModel by di()
    abstract fun onShot(sockets: Collection<PlayerSocket>, action: ShotAction)
    abstract fun onHit(sockets: Collection<PlayerSocket>, action: HitAction)
    abstract fun onEmpty(action: EmptyAction)
    abstract fun onDefeat(action: DefeatAction)
    abstract fun onDisconnect(action: DisconnectAction)
    abstract fun onEndGame(action: BattleEndAction)
    abstract fun onMiss(sockets: Collection<PlayerSocket>, action: MissAction)
    abstract fun onConnect(sockets: PlayersSockets, connectionAction: ConnectionAction)
    abstract fun onMessage(action: Action)
    abstract fun onTurn(action: TurnAction)
    abstract fun onReady(action: PlayerReadinessAction)
    abstract fun onFleetSettings(action: FleetSettingsAction)
    abstract fun onPlayers(action: ConnectedPlayersAction)


}
