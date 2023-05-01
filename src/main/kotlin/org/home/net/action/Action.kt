package org.home.net.action

import org.home.mvc.model.BattleModel
import org.home.net.action.ActionType.*
import org.home.mvc.model.Coord
import org.home.net.message.Message
import org.home.utils.RomansDigits
import org.home.utils.extensions.BooleansExtensions.then

enum class ActionType {
    HIT,
    SHOT,
    CONNECT,
    DISCONNECT,
    SHIP_CREATION,
    SHIP_DELETION,
    LEAVE_BATTLE,
    NEW_SERVER,
    NEW_SERVER_ESTABLISHED,
    NEW_SERVER_CONNECTION,
    DEFEAT,
    BATTLE_ENDED,
    READY,
    NOT_READY,
    TURN,
    PLAYERS,
    READY_PLAYERS,
    FLEETS_READINESS,
    MISS,
    EMPTY,
    BATTLE_STARTED,
    FLEET_SETTINGS
}

interface HasText { val text: String }
abstract class Action(val type: ActionType): Message

open class TextAction(type: ActionType, override val text: String): Action(type), HasText {
    override fun toString() = "TextAction($type, $text)"
}

abstract class PlayerAction(type: ActionType, val player: String): Action(type) {
    override fun toString() = "PlayerAction($type, '$player')"
}

class ConnectionAction(player: String): PlayerAction(CONNECT, player = player)

class ShipDeletionAction(val shipType: Int, player: String): PlayerAction(SHIP_DELETION, player){
    override fun toString() = "ShipCountAction($player: -${RomansDigits.arabicToRoman(shipType)})"
}

class ShipConstructionAction(val shipType: Int, player: String): PlayerAction(SHIP_CREATION, player){
    override fun toString() = "ShipDiscountAction($player: +${RomansDigits.arabicToRoman(shipType)})"
}

class FleetSettingsAction(
    val height: Int, val width: Int, val shipsTypes: Map<Int, Int>): Action(FLEET_SETTINGS)
{
    constructor(model: BattleModel) : this(
        model.height.value,
        model.width.value,
        model.battleShipsTypes.value.toMutableMap()
    )

    override fun toString() = "FleetSettingsAction(height=$height, width=$width, shipsTypes=$shipsTypes)"
}

open class ConnectedPlayersAction(val players: Collection<String>): Action(PLAYERS) {
    override fun toString() = "PlayersAction($type, players=$players)"
}

abstract class PlayerReadinessAction(type: ActionType, player: String): PlayerAction(type, player = player) {
    val isReady get() = type == READY
}

class ReadyAction(readyPlayer: String): PlayerReadinessAction(READY, player = readyPlayer)
class NotReadyAction(readyPlayer: String): PlayerReadinessAction(NOT_READY, player = readyPlayer)

class AreReadyAction(val players: Collection<String>): Action(READY_PLAYERS) {
    override fun toString() = "ReadyPlayersAction($type, players=$players)"
}

class FleetsReadinessAction(val states: Map<String, Map<Int, Int>>): Action(FLEETS_READINESS) {
    override fun toString() = "FleetsReadinessAction($states)"
}

class TurnAction(player: String): PlayerAction(TURN, player = player)

abstract class HasAShot(actionType: ActionType, shooter: String): PlayerAction(actionType, shooter) {
    abstract val shot: Coord
    abstract val target: String
    override fun toString() = "$type: '$player' --$shot->> '$target'"
}

class ShotAction(override val shot: Coord,
                 player: String,
                 override val target: String): HasAShot(SHOT, player)

class HitAction(hit: Coord,
                player: String,
                override val target: String): HasAShot(HIT, player)
{
    constructor(shotAction: ShotAction): this(shotAction.shot, shotAction.player, shotAction.target)
    override val shot: Coord = hit
}

class MissAction(miss: Coord,
                 player: String,
                 override val target: String): HasAShot(MISS, player)
{
    constructor(shotAction: ShotAction): this(shotAction.shot, shotAction.player, shotAction.target)
    override val shot: Coord = miss
}

sealed class PlayerToRemoveAction(type: ActionType, player: String): PlayerAction(type, player) {
    val isDefeat get() = (this is DefeatAction).then { this as DefeatAction }
}

class LeaveAction(player: String): PlayerToRemoveAction(LEAVE_BATTLE, player = player)
class DisconnectAction(player: String): PlayerToRemoveAction(DISCONNECT, player = player)
class DefeatAction(val shooter: String, defeated: String): PlayerToRemoveAction(DEFEAT, player = defeated)

class NewServerAction(player: String) : PlayerAction(NEW_SERVER, player)

class NewServerConnectionAction(player: String, val ip: String, val port: Int): PlayerAction(NEW_SERVER_CONNECTION, player)
object EmptyAction: Action(EMPTY) { override fun toString() = "EmptyAction($type)" }
object BattleStartAction: Action(BATTLE_STARTED) { override fun toString() = "BattleStartedAction($type)" }

class BattleEndAction(winner: String): PlayerAction(BATTLE_ENDED, player = winner)





