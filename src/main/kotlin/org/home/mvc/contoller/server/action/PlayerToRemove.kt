package org.home.mvc.contoller.server.action

sealed class PlayerToRemoveAction(player: String): PlayerAction(player)
class LeaveAction(player: String): PlayerToRemoveAction(player = player)
class DisconnectAction(player: String): PlayerToRemoveAction(player = player)
class DefeatAction(val shooter: String, defeated: String): PlayerToRemoveAction(player = defeated)