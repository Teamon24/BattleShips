package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.NewServerAction
import org.home.mvc.contoller.server.action.NewServerConnectionAction

class NewServerReceived(action: NewServerAction): HasAPlayer(action.player) {
    val turnList: List<String> = action.turnList
    val readyPlayers: Set<String> = action.readyPlayers
}
class NewServerConnectionReceived(val action: NewServerConnectionAction): BattleEvent()