package org.home.net.message

import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleIsStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.PlayerWasDisconnected
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Coord
import org.home.utils.RomansDigits
import org.home.utils.extensions.BooleansExtensions.then

sealed class Action: Message {
    override fun toString(): String {
        return when(this) {
            is PlayerConnectionAction -> "Connection($player)"
            is PlayersConnectionsAction -> "Connections($players)"
            is ReadyAction -> "Ready($player)"
            is NotReadyAction -> "NotReady($player)"
            is FleetsReadinessAction -> "FleetsReadiness${states})"
            is AreReadyAction -> "AreReady(${players})"
            is TurnAction -> "Turn($player)"
            is ShotAction -> "Shot(${this.toStr})"
            is HitAction -> "Hit(${this.toStr})"
            is MissAction -> "Miss(${this.toStr})"
            is DefeatAction -> "Defeat(${player})"
            is LeaveAction -> "Leave(${player})"
            is DisconnectAction -> "Disconnect(${player})"
            is BattleStartAction -> "BattleStart"
            is BattleEndAction -> "BattleEnd(winner=$player)"
            is NewServerAction -> "NewServer(${player})"
            is NewServerConnectionAction -> run { "NewServerConnection($player $ip:$port)" }
            is ShipAdditionAction -> "ShipAddition[$player: $op${RomansDigits.arabicToRoman(shipType)}]"
            is ShipDeletionAction -> "ShipDeletion[$player: $op${RomansDigits.arabicToRoman(shipType)}]"
            is FleetSettingsAction -> buildString {
                append("FleetSettings(width=").append(width)
                append(" height=").append(height)
                append(" types=").append(shipsTypes)
                append(")")
            }
        }
    }
}


sealed class PlayerAction(val player: String): Action()

class PlayerConnectionAction(player: String): PlayerAction(player = player)

sealed class ShipAction(val shipType: Int, player: String): PlayerAction(player) {
    abstract val op: String
}

class ShipDeletionAction(shipType: Int, player: String): ShipAction(shipType, player) {
    override val op get() = "-"
}

class ShipAdditionAction(shipType: Int, player: String): ShipAction(shipType, player) {
    override val op get() = "+"
}

class FleetSettingsAction(
    val height: Int,
    val width: Int,
    val shipsTypes: Map<Int, Int>): Action()
{
    constructor(model: BattleModel) : this(
        model.height.value,
        model.width.value,
        model.battleShipsTypes.value.toMutableMap()
    )
}

open class PlayersConnectionsAction(val players: Collection<String>): Action()

sealed class PlayerReadinessAction(player: String): PlayerAction(player = player) {
    val isReady get() = this is ReadyAction
}

class ReadyAction(readyPlayer: String): PlayerReadinessAction(player = readyPlayer)
class NotReadyAction(readyPlayer: String): PlayerReadinessAction(player = readyPlayer)

class AreReadyAction(val players: Collection<String>): Action()

class FleetsReadinessAction(val states: Map<String, Map<Int, Int>>): Action()

class TurnAction(player: String): PlayerAction(player = player)

sealed class HasAShot(shooter: String, val shot: Coord): PlayerAction(shooter) {
    abstract val target: String
    val toStr get() = "$player' --$shot->> '$target'"
}

class ShotAction(shot: Coord, player: String, override val target: String): HasAShot(player, shot) {
    fun hit() = HitAction(shot, player, target)
}

class HitAction(hit: Coord, player: String, override val target: String): HasAShot(player, hit) {
    constructor(shotAction: ShotAction): this(shotAction.shot, shotAction.player, shotAction.target)
}

class MissAction(miss: Coord, player: String, override val target: String): HasAShot(player, miss) {
    constructor(shotAction: ShotAction): this(shotAction.shot, shotAction.player, shotAction.target)
}

sealed class PlayerToRemoveAction(player: String): PlayerAction(player) {
    val asDefeat get() = (this is DefeatAction).then { this as DefeatAction }
}

class LeaveAction(player: String): PlayerToRemoveAction(player = player)
class DisconnectAction(player: String): PlayerToRemoveAction(player = player)
class DefeatAction(val shooter: String, defeated: String): PlayerToRemoveAction(player = defeated)

class NewServerAction(player: String) : PlayerAction(player)

class NewServerConnectionAction(player: String, val ip: String, val port: Int):
    PlayerAction(player)
object BattleStartAction: Action()

class BattleEndAction(winner: String): PlayerAction(player = winner)

val Action.event get() = when (this) {
    is BattleEndAction -> BattleIsEnded(this)
    is BattleStartAction -> BattleIsStarted
    is PlayerConnectionAction -> ConnectedPlayerReceived(this)
    is DefeatAction -> PlayerWasDefeated(this)
    is DisconnectAction -> PlayerWasDisconnected(this)
    is FleetsReadinessAction -> FleetsReadinessReceived(this)
    is HitAction -> ShipWasHit(this)
    is LeaveAction -> PlayerLeaved(this)
    is MissAction -> ThereWasAMiss(this)
    is NewServerAction -> NewServerReceived(this)
    is NewServerConnectionAction -> NewServerConnectionReceived(this)
    is PlayersConnectionsAction -> ConnectedPlayersReceived(this)
    is AreReadyAction -> ReadyPlayersReceived(this)
    is ShipAdditionAction -> ShipWasAdded(this)
    is ShipDeletionAction -> ShipWasDeleted(this)
    is TurnAction -> TurnReceived(this)
    else -> null //НЕ ТРОГАТЬ! На это значение ориентируется логика обработки полученных сообщений.
}



