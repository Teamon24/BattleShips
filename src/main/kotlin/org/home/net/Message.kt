package org.home.net

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import org.home.mvc.model.BattleModel
import org.home.net.ActionType.*
import java.io.Serializable

enum class ActionType {
    HIT,
    SHOT,
    CONNECT,
    DISCONNECT,
    DEFEAT,
    ENDGAME,
    TURN,
    MISS,
    EMPTY,
    FLEET_SETTINGS,
    SUCCESS
}

interface Message: Serializable

interface HasText {
    val text: String
}

open class ActionMessage(val actionType: ActionType): Message

open class TextMessage(actionType: ActionType, override val text: String): ActionMessage(actionType), HasText {
    override fun toString() = "TextMessage($actionType, $text)"
}


open class PlayerMessage(actionType: ActionType, val player: String): ActionMessage(actionType) {
    override fun toString(): String {
        return "PlayerMessage($actionType, '$player')"
    }
}

class ShotMessage(
    val letter: String,
    val number: Int,
    player: String,
    val target: String,
):
    PlayerMessage(SHOT, player)

class HitMessage(
    val letter: String,
    val number: Int,
    player: String,
    val target: String,
):
    PlayerMessage(HIT, player)


class DisconnectMessage(player: String): PlayerMessage(DISCONNECT, player = player)
class ConnectMessage(player: String): PlayerMessage(CONNECT, player = player)
class DefeatMessage(defeated: String): PlayerMessage(DEFEAT, player = defeated)
class EndGameMessage(winner: String): PlayerMessage(ENDGAME, player = winner)
class TurnMessage(player: String): PlayerMessage(TURN, player = player)

class PlayersMessage(val players: List<String>): ActionMessage(TURN) {

    constructor(players: SimpleListProperty<String>): this(players.value.toList())
    override fun toString(): String {
        return "PlayersMessage($actionType, players=$players)"
    }
}

class FleetSettingsMessage(
    val height: Int, val width: Int, val shipsTypes: Map<Int, Int>): ActionMessage(FLEET_SETTINGS)
{
    constructor(model: BattleModel) : this(
        model.height.value, model.width.value, model.battleShipsTypes.value.toMutableMap())

    override fun toString(): String {
        return "FleetSettingsMessage(height=$height, width=$width, shipsTypes=$shipsTypes)"
    }
}


class SuccessConnection(player: String): TextMessage(SUCCESS, text = "\"$player\" has been connected")

object MissMessage: ActionMessage(MISS)

object EmptyMessage: ActionMessage(EMPTY) {
    override fun toString(): String {
        return "EmptyMessage($actionType)"
    }
}



