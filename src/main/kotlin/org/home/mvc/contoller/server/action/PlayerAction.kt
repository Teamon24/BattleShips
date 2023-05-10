package org.home.mvc.contoller.server.action

sealed class PlayerAction(val player: String): Action()

class ConnectionAction(player: String): PlayerAction(player = player)
class ConnectionsAction(val players: Collection<String>): Action()

sealed class PlayerReadinessAction(player: String): PlayerAction(player = player) {
    val isReady get() = this is ReadyAction
}

class ReadyAction(readyPlayer: String): PlayerReadinessAction(player = readyPlayer)
class NotReadyAction(readyPlayer: String): PlayerReadinessAction(player = readyPlayer)
class AreReadyAction(val players: Collection<String>): Action()

