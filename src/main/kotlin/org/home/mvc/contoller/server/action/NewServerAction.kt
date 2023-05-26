package org.home.mvc.contoller.server.action

import javafx.beans.property.SimpleSetProperty
import org.home.mvc.view.battle.subscription.NewServerInfo

class NewServerAction(player: String, val turnList: List<String> = emptyList(), readyPlayers: SimpleSetProperty<String>) : PlayerAction(player) {
    val readyPlayers = readyPlayers.toMutableSet()
}

class NewServerConnectionAction(val newServer: NewServerInfo): PlayerAction(newServer.player) {
    fun toStringPart() = "$newServer"
}