package org.home.net

import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.utils.extensions.exclude

object MessagesDSL {

    fun messages(addAll: MutableCollection<Message>.() -> Unit): Messages<Message> {
        val messages = mutableListOf<Message>()
        messages.addAll()
        return Messages(MessagesInfo(messages.size) + messages)
    }

    @JvmName("message")
    fun Message.wrap(): Messages<Message> {
        val messages = mutableListOf<Message>()
        messages.add(this)
        return Messages(MessagesInfo(1) + messages)
    }

    fun MutableCollection<Message>.fleetSettings(model: BattleModel) {
        this.add(FleetSettingsAction(model))
    }

    fun MutableCollection<Message>.playersExcept(player: String, model: BattleModel) {
        this.add(PlayersAction(model.playersNames.exclude(player)))
    }

    fun MutableCollection<Message>.readyPlayers(model: BattleModel) {
        this.add(ReadyPlayersAction(model.readyPlayers.thoseAreReady))
    }

    operator fun MessagesInfo.plus(messages: Collection<Message>): List<Message> {
        return mutableListOf(this as Message).apply { addAll(messages) }
    }
}
