package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.NewServerConnectionAction
import org.home.mvc.contoller.server.action.PlayerAction

class NewServerReceived(action: PlayerAction): HasAPlayer(action.player)
class NewServerConnectionReceived(val action: NewServerConnectionAction): BattleEvent()