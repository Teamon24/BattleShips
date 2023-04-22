package org.home.mvc.contoller

import org.home.ApplicationProperties
import org.home.mvc.model.BattleModel
import org.home.net.ConnectAction
import org.home.net.DefeatAction
import org.home.net.DisconnectAction
import org.home.net.EmptyAction
import org.home.net.EndGameAction
import org.home.net.FleetSettingsAction
import org.home.net.HitAction
import org.home.net.Action
import org.home.net.MissAction
import org.home.net.PlayerSocket
import org.home.net.PlayersAction
import org.home.net.ReadyAction
import org.home.net.ShotAction
import org.home.net.TurnAction
import tornadofx.Controller

abstract class GameTypeController: Controller() {
    protected val applicationProperties: ApplicationProperties by di()
    protected val model: BattleModel by di()
    abstract fun onShot(msg: ShotAction)
    abstract fun onHit(msg: HitAction)
    abstract fun onEmpty(msg: EmptyAction)
    abstract fun onDefeat(msg: DefeatAction)
    abstract fun onDisconnect(msg: DisconnectAction)
    abstract fun onEndGame(msg: EndGameAction)
    abstract fun onMiss(msg: MissAction)
    abstract fun onConnect(socket: PlayerSocket, sockets: Collection<PlayerSocket>, connectAction: ConnectAction)
    abstract fun onMessage(msg: Action)
    abstract fun onTurn(msg: TurnAction)
    abstract fun onReady(msg: ReadyAction): TurnAction?
    abstract fun onFleetSettings(msg: FleetSettingsAction)
    abstract fun onPlayers(msg: PlayersAction)
    abstract fun chooseTurn(model: BattleModel): TurnAction
}
