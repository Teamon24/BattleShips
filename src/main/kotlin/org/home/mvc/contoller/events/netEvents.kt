package org.home.mvc.contoller.events

import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.view.Scopes
import org.home.net.message.AreReadyAction
import org.home.net.message.BattleEndAction
import org.home.net.message.PlayersConnectionsAction
import org.home.net.message.PlayerConnectionAction
import org.home.net.message.FleetSettingsAction
import org.home.net.message.FleetsReadinessAction
import org.home.net.message.HasAShot
import org.home.net.message.HitAction
import org.home.net.message.MissAction
import org.home.net.message.NewServerConnectionAction
import org.home.net.message.PlayerAction
import org.home.net.message.PlayerReadinessAction
import org.home.net.message.ShipAction
import org.home.utils.DSLContainer
import org.home.utils.RomansDigits
import org.home.utils.dslContainer
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.log
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.Scope

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
sealed class HasAPlayer(val player: String): BattleEvent()

sealed class HasPlayers(val players: Collection<String>): BattleEvent()
class ConnectedPlayerReceived(action: PlayerConnectionAction): HasAPlayer(action.player)


class FleetSettingsReceived(val settings: FleetSettingsAction): BattleEvent()
class PlayerIsNotReadyReceived(action: PlayerReadinessAction): HasAPlayer(action.player)
class PlayerIsReadyReceived(action: PlayerReadinessAction): HasAPlayer(action.player)

class FleetsReadinessReceived(action: FleetsReadinessAction): BattleEvent() {
    val states = action.states
}
class ConnectedPlayersReceived(action: PlayersConnectionsAction): HasPlayers(action.players)
class ReadyPlayersReceived(action: AreReadyAction): HasPlayers(action.players)

class TurnReceived(action: PlayerAction): HasAPlayer(action.player)

sealed class ThereWasAShot(val hasAShot: HasAShot): HasAPlayer(hasAShot.player) {
    fun isMiss() = hasAShot is MissAction
}
class ShipWasHit(hitAction: HitAction): ThereWasAShot(hitAction)

class ThereWasAMiss(missAction: MissAction): ThereWasAShot(missAction)
sealed class PlayerToRemoveReceived(action: PlayerAction): HasAPlayer(action.player)
class PlayerWasDefeated(action: PlayerAction): PlayerToRemoveReceived(action)
class PlayerLeaved(action: PlayerAction): PlayerToRemoveReceived(action)

class PlayerWasDisconnected(action: PlayerAction) : PlayerToRemoveReceived(action)
object BattleIsStarted: BattleEvent()

class BattleIsEnded(winner: String): HasAPlayer(winner) {
    constructor(battleEndAction: BattleEndAction): this(battleEndAction.player)
}
class NewServerReceived(action: PlayerAction): HasAPlayer(action.player)

class NewServerConnectionReceived(val action: NewServerConnectionAction): BattleEvent()

sealed class FleetEditEvent(val shipType: Int, player: String): HasAPlayer(player) {
    abstract val operation: SimpleIntegerProperty.() -> Unit
    abstract val op: String
}

class ShipWasAdded(shipType: Int, player: String): FleetEditEvent(shipType, player) {
    constructor(action: ShipAction): this(action.shipType, action.player)
    override val operation: SimpleIntegerProperty.() -> Unit = { value -= 1 }
    override val op = "-"
}

class ShipWasDeleted(shipType: Int, player: String): FleetEditEvent(shipType, player) {
    constructor(action: ShipAction): this(action.shipType, action.player)
    override val operation: SimpleIntegerProperty.() -> Unit = { value += 1 }
    override val op = "+"
}



