package org.home.mvc.contoller.server.action

import home.extensions.AnysExtensions.name
import home.extensions.delete
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
import org.home.net.server.Message
import org.home.utils.RomansDigits

sealed class Action: Message {
    private inline val prefix get() = this::class.name.delete(Action::class.name)

    override fun toString(): String {
        return when(this) {
            is ConnectionAction -> "$prefix($player)"
            is ConnectionsAction -> "$prefix($players)"
            is ReadyAction -> "$prefix($player)"
            is NotReadyAction -> "$prefix($player)"
            is FleetsReadinessAction -> "$prefix${states})"
            is AreReadyAction -> "$prefix(${players})"
            is TurnAction -> "$prefix($player)"
            is ShotAction -> "$prefix(${this.toStr})"
            is HitAction -> "$prefix(${this.toStr})"
            is MissAction -> "$prefix(${this.toStr})"
            is DefeatAction -> "$prefix(${player})"
            is LeaveAction -> "$prefix(${player})"
            is DisconnectAction -> "$prefix(${player})"
            is BattleStartAction -> "$prefix"
            is BattleEndAction -> "$prefix(winner=$player)"
            is NewServerAction -> "$prefix(${player})"
            is NewServerConnectionAction -> run { "$prefix($player $ip:$port)" }
            is ShipAdditionAction -> "$prefix[$player: $op${RomansDigits.arabicToRoman(shipType)}]"
            is ShipDeletionAction -> "$prefix[$player: $op${RomansDigits.arabicToRoman(shipType)}]"
            is FleetSettingsAction -> buildString {
                append("$prefix(width=").append(width)
                append(" height=").append(height)
                append(" types=").append(shipsTypes)
                append(")")
            }
        }
    }
}


object BattleStartAction: Action()
class BattleEndAction(winner: String): PlayerAction(player = winner)
class TurnAction(player: String): PlayerAction(player = player)

inline val Action.event get() = when (this) {
    is BattleEndAction -> BattleIsEnded(this)
    is BattleStartAction -> BattleIsStarted
    is ConnectionAction -> ConnectedPlayerReceived(this)
    is DefeatAction -> PlayerWasDefeated(this)
    is DisconnectAction -> PlayerWasDisconnected(this)
    is FleetsReadinessAction -> FleetsReadinessReceived(this)
    is HitAction -> ShipWasHit(this)
    is LeaveAction -> PlayerLeaved(this)
    is MissAction -> ThereWasAMiss(this)
    is NewServerAction -> NewServerReceived(this)
    is NewServerConnectionAction -> NewServerConnectionReceived(this)
    is ConnectionsAction -> ConnectedPlayersReceived(this)
    is AreReadyAction -> ReadyPlayersReceived(this)
    is ShipAdditionAction -> ShipWasAdded(this)
    is ShipDeletionAction -> ShipWasDeleted(this)
    is TurnAction -> TurnReceived(this)
    else -> null //НЕ ТРОГАТЬ! На это значение ориентируется логика обработки полученных сообщений.
}



