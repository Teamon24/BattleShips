package org.home.mvc.contoller

import org.home.mvc.contoller.events.FleetSettingsAccepted
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.PlayersListAccepted
import org.home.net.ConnectMessage
import org.home.net.DefeatMessage
import org.home.net.DisconnectMessage
import org.home.net.EmptyMessage
import org.home.net.EndGameMessage
import org.home.net.HitMessage
import org.home.net.ActionMessage
import org.home.net.FleetSettingsMessage
import org.home.net.Message
import org.home.net.MissMessage
import org.home.net.PlayersMessage
import org.home.net.ShotMessage
import org.home.net.SuccessConnection
import org.home.net.TurnMessage
import org.home.utils.log
import org.home.utils.logger

class AllAgainstAllController : GameTypeController() {
    override fun onShot(msg: ShotMessage): ActionMessage { TODO("onShot") }
    override fun onHit(msg: HitMessage): ActionMessage { TODO("onHit") }
    override fun onEmpty(msg: EmptyMessage): ActionMessage { TODO("onEmpty") }
    override fun onDefeat(msg: DefeatMessage): ActionMessage { TODO("onDefeat") }
    override fun onDisconnect(msg: DisconnectMessage): ActionMessage { TODO("onDisconnect") }
    override fun onEndGame(msg: EndGameMessage): ActionMessage { TODO("onEndGame") }
    override fun onMiss(msg: MissMessage): ActionMessage { TODO("onMiss") }
    override fun onConnect(msg: ConnectMessage): ActionMessage {
        fire(PlayerWasConnected(msg.player))
        return SuccessConnection(msg.player)
    }

    override fun onMessage(msg: Message) { log { msg } }
    override fun onTurn(msg: TurnMessage) { TODO("onTurn") }
    override fun onFleetSettings(msg: FleetSettingsMessage) {
        fire(FleetSettingsAccepted(msg))
    }

    override fun onPlayers(msg: PlayersMessage) {
        fire(PlayersListAccepted(msg.players))
    }
}