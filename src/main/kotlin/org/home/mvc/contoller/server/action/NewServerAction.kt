package org.home.mvc.contoller.server.action

import org.home.mvc.view.battle.subscription.NewServerInfo

class NewServerAction(player: String, val turnList: List<String> = emptyList()) : PlayerAction(player)
class NewServerConnectionAction(newServer: NewServerInfo): PlayerAction(newServer.player) {
    val ip: String = newServer.ip
    val port: Int = newServer.port
    fun string() = "[$player] $ip:$port"
}