package org.home.mvc.contoller.events

import org.home.mvc.contoller.server.action.AreReadyAction
import org.home.mvc.contoller.server.action.ConnectionAction
import org.home.mvc.contoller.server.action.PlayerReadinessAction
import org.home.mvc.contoller.server.action.ConnectionsAction

sealed class HasAPlayer(val player: String): BattleEvent()
sealed class HasPlayers(val players: Collection<String>): BattleEvent()
class ConnectedPlayerReceived(action: ConnectionAction): HasAPlayer(action.player)
class PlayerIsNotReadyReceived(action: PlayerReadinessAction): HasAPlayer(action.player)
class PlayerIsReadyReceived(action: PlayerReadinessAction): HasAPlayer(action.player)
class ConnectedPlayersReceived(action: ConnectionsAction): HasPlayers(action.players)
class ReadyPlayersReceived(action: AreReadyAction): HasPlayers(action.players)


