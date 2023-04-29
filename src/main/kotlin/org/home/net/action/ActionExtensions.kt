package org.home.net.action

import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.net.message.Message
import org.home.utils.extensions.CollectionsExtensions.exclude

object ActionExtensions {
    fun MutableCollection<Message>.ready(readyPlayer: String) = this.add(ReadyAction(readyPlayer))
    fun MutableCollection<Message>.battleStarted() = this.add(BattleStartAction)
    fun MutableCollection<Message>.turn(player: String) = this.add(TurnAction(player))
    fun MutableCollection<Message>.miss(shotAction: ShotAction) = this.add(MissAction(shotAction))
    fun MutableCollection<Message>.miss(missAction: MissAction) = this.add(missAction)
    fun MutableCollection<Message>.hit(hitAction: HitAction) = this.add(hitAction)
    fun MutableCollection<Message>.defeat(player: String) = this.add(DefeatAction(player))
    fun MutableCollection<Message>.fleetSettings(model: BattleModel) = this.add(FleetSettingsAction(model))

    fun MutableCollection<Message>.fleetsReadinessExcept(connectedPlayer: String, model: BattleModel): Boolean {
        val fleetsReadinessAction = fleetsReadinessAction(connectedPlayer, model)
        return this.add(fleetsReadinessAction)
    }

    fun fleetsReadinessAction(
        connectedPlayer: String,
        model: BattleModel,
    ): FleetsReadinessAction {
        val states = model.fleetsReadiness
            .exclude(connectedPlayer)
            .map { (player, state) ->
                player to state.map { (shipType, number) -> shipType to number.value }.toMap()
            }
            .toMap()

        val fleetsReadinessAction = FleetsReadinessAction(states)
        return fleetsReadinessAction
    }

    fun MutableCollection<Message>.connectedPlayersExcept(player: String, model: BattleModel) {
        val connectionsAction = this@ActionExtensions.connectedPlayersExcept(player, model)
        this.add(connectionsAction)
    }

    fun connectedPlayersExcept(
        player: String,
        model: BattleModel,
    ): ConnectionsAction {
        return ConnectionsAction(model.playersNames.exclude(player))
    }

    fun MutableCollection<Message>.readyPlayers(model: BattleModel) {
        val readyPlayers = this@ActionExtensions.readyPlayers(model)
        this.add(readyPlayers)
    }

    fun readyPlayers(model: BattleModel): AreReadyAction {
        return AreReadyAction(model.playersReadiness.thoseAreReady)
    }
}
