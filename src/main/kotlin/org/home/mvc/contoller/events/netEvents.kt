package org.home.mvc.contoller.events

import org.home.net.action.ActionType
import org.home.net.action.BattleEndAction
import org.home.net.action.ConnectionAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.FleetsReadinessAction
import org.home.net.action.HasAShot
import org.home.net.action.HitAction
import org.home.net.action.MissAction
import org.home.net.action.NewServerConnectionAction
import tornadofx.FXEvent

sealed class HasAPlayer(val player: String): FXEvent()

class ConnectedPlayerReceived(connectionAction: ConnectionAction): HasAPlayer(connectionAction.player)  {
    override fun toString() = "Connected($player)" }


class FleetSettingsReceived(val settings: FleetSettingsAction): FXEvent() {
    override fun toString() = "FleetSettingsReceived($settings)"
}

class ConnectedPlayersReceived(val players: Collection<String>): FXEvent() {
    override fun toString() = "Connected($players)"
}

class PlayerIsNotReadyReceived(val player: String): FXEvent() { override fun toString() = "IsNotReady($player)"}
class PlayerIsReadyReceived(val player: String): FXEvent() { override fun toString() = "IsReady($player)"}

class FleetsReadinessReceived(action: FleetsReadinessAction): FXEvent() {
    val states = action.states
    override fun toString() = "FleetsReadiness${states})"
}

class ReadyPlayersReceived(val readyPlayers: Collection<String>): FXEvent() {
    override fun toString() = "Ready($readyPlayers)"
}

class TurnReceived(player: String): HasAPlayer(player) {
    override fun toString() = "Turn($player)"
}

sealed class ThereWasAShot(val hasAShot: HasAShot): FXEvent() {
    fun isMiss() = hasAShot.type == ActionType.MISS
    fun isHit() = hasAShot.type == ActionType.HIT
}

class ShipWasHit(hitAction: HitAction): ThereWasAShot(hitAction)
class ThereWasAMiss(missAction: MissAction): ThereWasAShot(missAction)

sealed class PlayerToRemoveReceived(val player: String): FXEvent()
class PlayerWasDefeated(player: String): PlayerToRemoveReceived(player) { override fun toString() = "Defeated($player)"}
class PlayerLeaved(player: String): PlayerToRemoveReceived(player) { override fun toString() = "Leaved($player)"}
class PlayerWasDisconnected(player: String) : PlayerToRemoveReceived(player) {
    override fun toString() = "Disconnected($player)"
}

object BattleStarted: FXEvent()
class BattleIsEnded(battleEndAction: BattleEndAction): HasAPlayer(battleEndAction.player)

class NewServerReceived(player: String): HasAPlayer(player)
class NewServerConnectionReceived(val action: NewServerConnectionAction): FXEvent()
