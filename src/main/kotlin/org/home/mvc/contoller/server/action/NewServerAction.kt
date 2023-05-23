package org.home.mvc.contoller.server.action

import org.home.mvc.view.battle.subscription.NewServerInfo

class NewServerAction(player: String, val turnList: List<String> = emptyList(), val readyPlayers: Set<String>) : PlayerAction(player)
class NewServerConnectionAction(val newServer: NewServerInfo): PlayerAction(newServer.player) {
    fun toStringPart() = "$newServer"
}