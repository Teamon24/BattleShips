package org.home.mvc.contoller.events

import home.extensions.AnysExtensions.invoke
import org.home.app.di.FxScopes
import org.home.mvc.contoller.server.action.BattleEndAction
import org.home.mvc.contoller.server.action.TurnAction
import org.home.utils.DSLContainer
import org.home.utils.RomansDigits
import org.home.utils.dslElements
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.Scope

inline infix fun Component.eventbus(event: BattleEvent) = fire(event)

inline fun Component.eventbus(addEvents: DSLContainer<BattleEvent>.() -> Unit) {
    dslElements(addEvents).forEach(this::fire)
}

sealed class BattleEvent: FXEvent() {

    override val scope: Scope get() = FxScopes.getGameScope()

    override fun toString(): String {
        return when(this) {
            is ConnectedPlayerReceived -> "Connected($player)"
            is ConnectedPlayersReceived -> "AreConnected($players)"
            is PlayerIsNotReadyReceived -> "IsNotReady($player)"
            is PlayerIsReadyReceived -> "IsReady($player)"
            is FleetsReadinessReceived -> "GotFleetsReadiness$states"
            is ReadyPlayersReceived -> "AreReady($players)"
            is TurnReceived -> "GotATurn($player)"
            is ShipWasHit -> "GotHit(${hasAShot.toStr})"
            is ShipWasSunk -> "GotSunk(${hasAShot.toStr})"
            is ThereWasAMiss -> "Missed(${hasAShot.toStr})"
            is PlayerWasDefeated -> "Defeated($player)"
            is PlayerLeaved -> "Leaved($player)"
            is PlayerWasDisconnected -> "Disconnected($player)"
            is BattleIsStarted -> "BattleIsStarted"
            is BattleIsContinued -> "BattleIsContinued"
            is BattleIsEnded -> "BattleIsEnded(winner=$player)"
            is NewServerReceived -> "GotNewServer($player)"
            is NewServerConnectionReceived -> "GotNewServerConnection(${action.toStringPart()})"
            is ShipWasAdded -> "[$player: $opSign${RomansDigits.arabicToRoman(shipType)}]"
            is ShipWasDeleted -> "[$player: $opSign${RomansDigits.arabicToRoman(shipType)}]"

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
object BattleIsContinued: BattleEvent()
class BattleIsEnded(winner: String): HasAPlayer(winner) {
    constructor(battleEndAction: BattleEndAction): this(battleEndAction.player)
}
class TurnReceived(turnPlayer: String): HasAPlayer(turnPlayer) {
    constructor(action: TurnAction) : this(action.player)
}


