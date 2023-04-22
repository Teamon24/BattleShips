package org.home.mvc.contoller

import org.home.ApplicationProperties
import org.home.mvc.model.BattleModel
import org.home.net.ActionMessage
import org.home.net.*
import tornadofx.Controller
import java.net.Socket

abstract class GameTypeController: Controller() {
    protected val applicationProperties: ApplicationProperties by di()
    protected val model: BattleModel by di()
    abstract fun onShot(msg: ShotMessage): ActionMessage
    abstract fun onHit(msg: HitMessage)
    abstract fun onEmpty(msg: EmptyMessage): ActionMessage
    abstract fun onDefeat(msg: DefeatMessage): ActionMessage
    abstract fun onDisconnect(msg: DisconnectMessage): ActionMessage
    abstract fun onEndGame(msg: EndGameMessage): ActionMessage
    abstract fun onMiss(msg: MissMessage): ActionMessage
    abstract fun onConnect(socket: PlayerSocket, sockets: Collection<PlayerSocket>, msg: ConnectMessage)
    abstract fun onMessage(msg: Message)
    abstract fun onTurn(msg: TurnMessage)
    abstract fun onReady(msg: ReadyMessage): TurnMessage?
    abstract fun onFleetSettings(msg: FleetSettingsMessage)
    abstract fun onPlayers(msg: PlayersMessage)
    abstract fun chooseTurn(model: BattleModel): TurnMessage
}
