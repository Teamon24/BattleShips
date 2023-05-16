package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.AreReadyAction
import org.home.mvc.contoller.server.action.ConnectionAction
import org.home.mvc.contoller.server.action.PlayerReadinessAction
import org.home.mvc.contoller.server.action.ConnectionsAction
import org.home.mvc.contoller.server.action.NotReadyAction
import org.home.mvc.contoller.server.action.ReadyAction

sealed class HasAPlayer(val player: String): BattleEvent()
sealed class HasPlayers(val players: Collection<String>): BattleEvent()

class ConnectedPlayerReceived(action: ConnectionAction): HasAPlayer(action.player)

sealed class PlayerReadinessReceived(player: String): HasAPlayer(player)

class PlayerIsNotReadyReceived(action: PlayerReadinessAction): PlayerReadinessReceived(action.player) {
    constructor(player: String) : this(NotReadyAction(player))
}
class PlayerIsReadyReceived(action: PlayerReadinessAction): PlayerReadinessReceived(action.player) {
    constructor(player: String) : this(ReadyAction(player))
}

class ConnectedPlayersReceived(action: ConnectionsAction): HasPlayers(action.players)
class ReadyPlayersReceived(action: AreReadyAction): HasPlayers(action.players)


