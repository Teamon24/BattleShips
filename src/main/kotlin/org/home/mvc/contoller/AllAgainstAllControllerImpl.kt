package org.home.mvc.contoller

import org.home.net.ConnectMessage
import org.home.net.DefeatMessage
import org.home.net.DisconnectMessage
import org.home.net.EmptyMessage
import org.home.net.EndGameMessage
import org.home.net.HitMessage
import org.home.net.ActionMessage
import org.home.net.Message
import org.home.net.MissMessage
import org.home.net.ShotMessage
import org.home.net.SuccessConnection

class AllAgainstAllController : GameTypeController() {
    override fun onShot(msg: ShotMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onHit(msg: HitMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onEmpty(msg: EmptyMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onDefeat(msg: DefeatMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onDisconnect(msg: DisconnectMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onEndGame(msg: EndGameMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onMiss(msg: MissMessage): ActionMessage {
        TODO("Not yet implemented")
    }

    override fun onConnect(msg: ConnectMessage): ActionMessage {
        fire(PlayerWasConnected(msg.player))
        return SuccessConnection(msg.player)
    }

    override fun onMessage(msg: Message): Message {
        TODO("Not yet implemented")
    }

}