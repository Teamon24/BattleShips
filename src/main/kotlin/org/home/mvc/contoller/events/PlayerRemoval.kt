package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.PlayerAction

sealed class PlayerToRemoveReceived(action: PlayerAction): HasAPlayer(action.player)
class PlayerWasDefeated(action: PlayerAction): PlayerToRemoveReceived(action)
class PlayerLeaved(action: PlayerAction): PlayerToRemoveReceived(action)
class PlayerWasDisconnected(action: PlayerAction) : PlayerToRemoveReceived(action)