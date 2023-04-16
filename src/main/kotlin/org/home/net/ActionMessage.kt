package org.home.net

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
    SUCCESS
}

interface Message: Serializable

interface HasText {
    val text: String
}

open class ActionMessage(val actionType: ActionType): Message  {
    override fun toString() = "Message($actionType)"
}

open class TextMessage(actionType: ActionType, override val text: String): ActionMessage(actionType), HasText {
    override fun toString() = "Message($actionType, $text)"
}


open class PlayerMessage(actionType: ActionType, val player: String): ActionMessage(actionType)

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


class SuccessConnection(player: String): TextMessage(SUCCESS, text = "\"$player\" has been connected")

object MissMessage: ActionMessage(MISS)

object EmptyMessage: ActionMessage(EMPTY) {
    override fun toString(): String {
        return "Message($actionType)"
    }
}



