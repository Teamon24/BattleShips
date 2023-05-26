package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.NewServerAction
import org.home.mvc.contoller.server.action.NewServerConnectionAction

class NewServerReceived(val action: NewServerAction): HasAPlayer(action.player)
class NewServerConnectionReceived(val action: NewServerConnectionAction): BattleEvent()