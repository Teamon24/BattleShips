package org.home.mvc.contoller.server.action

import org.home.mvc.view.battle.subscriptions.NewServerInfo

class NewServerAction(player: String) : PlayerAction(player)
class NewServerConnectionAction(newServer: NewServerInfo): PlayerAction(newServer.player) {
    val ip: String = newServer.ip
    val port: Int = newServer.port
    fun string() = "[$player] $ip:$port"
}