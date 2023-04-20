package org.home.net

import org.home.mvc.model.BattleModel
import org.home.net.ActionType.*
import org.home.utils.aliases.Coord
import tornadofx.FXEvent
import java.io.Serializable

enum class ActionType {
    HIT,
    SHOT,
    CONNECT,
    DISCONNECT,
    DEFEAT,
    ENDGAME,
    READY,
    TURN,
    PLAYERS,
    MISS,
    EMPTY,
    FLEET_SETTINGS,
    CONNECTION_TIMEOUT
}

interface Message: Serializable { val actionType: ActionType }
interface HasText { val text: String }

open class ActionMessage(override val actionType: ActionType): Message

open class TextMessage(actionType: ActionType, override val text: String): ActionMessage(actionType), HasText {
    override fun toString() = "TextMessage($actionType, $text)"
}

open class PlayerMessage(actionType: ActionType, val player: String): ActionMessage(actionType) {
    override fun toString() = "PlayerMessage($actionType, '$player')"
}

class ShotMessage(val coord: Coord, player: String, val target: String): PlayerMessage(SHOT, player)

class HitMessage(val coord: Coord, player: String, val target: String): PlayerMessage(HIT, player)
class DisconnectMessage(player: String): PlayerMessage(DISCONNECT, player = player)

class ConnectMessage(player: String): PlayerMessage(CONNECT, player = player)
class DefeatMessage(defeated: String): PlayerMessage(DEFEAT, player = defeated)
class EndGameMessage(winner: String): PlayerMessage(ENDGAME, player = winner)
class ReadyMessage(readyPlayer: String): PlayerMessage(READY, player = readyPlayer)
open class TurnMessage(player: String): PlayerMessage(TURN, player = player)

class TeamTurnMessage(team: String): TurnMessage(player = team)
open class PlayersMessage(val players: Collection<String>): ActionMessage(PLAYERS) {
    override fun toString() = "PlayersMessage($actionType, players=$players)"
}

class ReadyPlayersMessage(players: Collection<String>): PlayersMessage(players) {
    override fun toString() = "ReadyPlayersMessage($actionType, players=$players)"
}

class FleetSettingsMessage(
    val height: Int, val width: Int, val shipsTypes: Map<Int, Int>): ActionMessage(FLEET_SETTINGS)
{
    constructor(model: BattleModel) : this(
        model.height.value, model.width.value, model.battleShipsTypes.value.toMutableMap())
    override fun toString() = "FleetSettingsMessage(height=$height, width=$width, shipsTypes=$shipsTypes)"

}

object MissMessage: ActionMessage(MISS)
object ConnectionTimeoutMessage : ActionMessage(CONNECTION_TIMEOUT)

object EmptyMessage: ActionMessage(EMPTY) {
    override fun toString() = "EmptyMessage($actionType)"
}



