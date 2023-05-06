package org.home.utils

import org.home.net.PlayerSocket
import org.home.net.message.Action
import org.home.net.message.MessagesDSL.Messages.Companion.withInfo
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.CollectionsExtensions.asMutableList


object PlayerSocketUtils {
    @JvmName("sendBatch")
    fun Collection<PlayerSocket>.send(
        first: MutableMap<String, MutableList<Action>>,
        vararg others: Map<String, List<Action>>,
    ) {
        sendToPlayers(merge(first, *others))
    }

    @JvmName("mergeBatch")
    private fun merge(
        first: MutableMap<String, MutableList<Action>>,
        vararg others: Map<String, List<Action>>,
    ): MutableMap<String, MutableList<Action>> {
        if (others.isEmpty()) {
            return first
        }

        return others.fold(first, ::mergeTwo)
    }

    @JvmName("sendToPlayersBatch")
    private fun Collection<PlayerSocket>.sendToPlayers(playersAndMessages: Map<String, List<Action>>) {
        playersAndMessages
            .map { it.key to withInfo(it.value) }
            .forEach { (player, messages) ->
                forEach { playerSocket ->
                    if (playerSocket.player == player) {
                        playerSocket.send(messages)
                    }
                }
            }
    }

    private fun mergeTwo(
        first: MutableMap<String, MutableList<Action>>,
        second: Map<String, List<Action>>,
    ): MutableMap<String, MutableList<Action>> {

        second.forEach { (player, shotMessages) ->
            first.merge(player, shotMessages.asMutableList()) { m1, m2 -> m1.apply { addAll(m2) } }
        }

        return first
    }
}