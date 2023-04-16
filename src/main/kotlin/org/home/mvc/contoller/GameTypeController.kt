package org.home.mvc.contoller

import org.home.net.ActionMessage
import org.home.net.*
import tornadofx.Controller

abstract class GameTypeController: Controller() {
    abstract fun onShot(msg: ShotMessage): ActionMessage
    abstract fun onHit(msg: HitMessage): ActionMessage
    abstract fun onEmpty(msg: EmptyMessage): ActionMessage
    abstract fun onDefeat(msg: DefeatMessage): ActionMessage
    abstract fun onDisconnect(msg: DisconnectMessage): ActionMessage
    abstract fun onEndGame(msg: EndGameMessage): ActionMessage
    abstract fun onMiss(msg: MissMessage): ActionMessage
    abstract fun onConnect(msg: ConnectMessage): ActionMessage
    abstract fun onMessage(msg: Message): Message
}
