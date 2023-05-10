package org.home.mvc.contoller.events

import org.home.app.di.Scopes
import org.home.mvc.contoller.server.action.BattleEndAction
import org.home.utils.DSLContainer
import org.home.utils.RomansDigits
import org.home.utils.dslContainer
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import org.home.mvc.contoller.server.action.PlayerAction
import org.home.utils.log
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.Scope

inline infix fun Component.eventbus(event: FXEvent) = fire(event)

inline fun Component.eventbus(addEvents: DSLContainer<FXEvent>.() -> Unit) {
    dslContainer(addEvents).forEach(this::fire)
}

sealed class BattleEvent: FXEvent() {

    override val scope: Scope
        get() {
            log { "scope of ${this.name} was requested" }
            return Scopes.gameScope
        }

    override fun toString(): String {
        return when(this) {
            is ConnectedPlayerReceived -> "Connected($player)"
            is ConnectedPlayersReceived -> "Connected($players)"
            is PlayerIsNotReadyReceived -> "IsNotReady($player)"
            is PlayerIsReadyReceived -> "IsReady($player)"
            is FleetsReadinessReceived -> "GotFleetsReadiness$states"
            is ReadyPlayersReceived -> "Ready($players)"
            is TurnReceived -> "GotATurn($player)"
            is ShipWasHit -> "GotHit(${hasAShot.toStr})"
            is ThereWasAMiss -> "Missed(${hasAShot.toStr})"
            is PlayerWasDefeated -> "Defeated($player)"
            is PlayerLeaved -> "Leaved($player)"
            is PlayerWasDisconnected -> "Disconnected($player)"
            is BattleIsStarted -> "BattleStarted"
            is BattleIsEnded -> "BattleIsEnded(winner=$player)"
            is NewServerReceived -> "GotNewServer($player)"
            is NewServerConnectionReceived -> action.run { "GotNewServerConnection($player $ip:$port)" }
            is ShipWasAdded -> "[$player: $op${RomansDigits.arabicToRoman(shipType)}]"
            is ShipWasDeleted -> "[$player: $op${RomansDigits.arabicToRoman(shipType)}]"

            is FleetSettingsReceived ->
                buildString {
                    settings {
                        append("FleetSettings(width=").append(width)
                        append("height=").append(height)
                        append("types=").append(shipsTypes)
                        append(")")
                    }
                }
        }
    }
}


object BattleIsStarted: BattleEvent()
class BattleIsEnded(winner: String): HasAPlayer(winner) {
    constructor(battleEndAction: BattleEndAction): this(battleEndAction.player)
}
class TurnReceived(action: PlayerAction): HasAPlayer(action.player)


