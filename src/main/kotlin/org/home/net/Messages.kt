package org.home.net

import org.home.mvc.model.BattleModel
import org.home.net.ActionType.*
import org.home.mvc.model.Coord
import org.home.mvc.model.thoseAreReady
import org.home.net.MessagesDSL.plus
import org.home.utils.extensions.exclude
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
    FLEET_SETTINGS
}

interface Message: Serializable

@JvmInline
value class Messages<T: Message>
internal constructor(val collection: Collection<T>) {
    fun forEach(function: (T) -> Unit) {
        collection.forEach { function(it) }
    }
}

class MessagesInfo(val number: Int): Message

interface Action: Message { val actionType: ActionType }
interface HasText { val text: String }

abstract class AbstractAction(override val actionType: ActionType): Action

open class TextAction(actionType: ActionType, override val text: String): AbstractAction(actionType), HasText {
    override fun toString() = "TextMessage($actionType, $text)"
}

open class PlayerAction(actionType: ActionType, val player: String): AbstractAction(actionType) {
    override fun toString() = "PlayerMessage($actionType, '$player')"
}

class ShotAction(val coord: Coord, player: String, val target: String): PlayerAction(SHOT, player)

class HitAction(val coord: Coord, player: String, val target: String): PlayerAction(HIT, player)
class DisconnectAction(player: String): PlayerAction(DISCONNECT, player = player)

class ConnectAction(player: String): PlayerAction(CONNECT, player = player)
class DefeatAction(defeated: String): PlayerAction(DEFEAT, player = defeated)
class EndGameAction(winner: String): PlayerAction(ENDGAME, player = winner)
class ReadyAction(readyPlayer: String): PlayerAction(READY, player = readyPlayer)
open class TurnAction(player: String): PlayerAction(TURN, player = player)

class TeamTurnAction(team: String): TurnAction(player = team)
open class PlayersAction(val players: Collection<String>): AbstractAction(PLAYERS) {
    override fun toString() = "PlayersMessage($actionType, players=$players)"
}

class ReadyPlayersAction(players: Collection<String>): PlayersAction(players) {
    override fun toString() = "ReadyPlayersMessage($actionType, players=$players)"
}

class FleetSettingsAction(
    val height: Int, val width: Int, val shipsTypes: Map<Int, Int>): AbstractAction(FLEET_SETTINGS)
{
    constructor(model: BattleModel) : this(
        model.height.value, model.width.value, model.battleShipsTypes.value.toMutableMap())
    override fun toString() = "FleetSettingsMessage(height=$height, width=$width, shipsTypes=$shipsTypes)"
}

object MissAction: AbstractAction(MISS)

object EmptyAction: AbstractAction(EMPTY) {
    override fun toString() = "EmptyMessage($actionType)"
}



