package org.home.utils

import org.home.mvc.contoller.server.PlayerSocket
import tornadofx.Dimension
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

typealias SocketMessages<M, S> = Pair<S, Collection<M>>
typealias SocketsMessages<M, S> = LinkedBlockingQueue<SocketMessages<M, S>>

typealias LinearUnits = Dimension<Dimension.LinearUnits>

typealias PlayersSockets = ConcurrentLinkedQueue<PlayerSocket>

object PlayersSocketsExtensions {
    operator fun PlayersSockets.get(player: String) =
        firstOrNull { it.player == player } ?:
            throw RuntimeException("There is no PlayerSocket for player \"$player\"")

    fun PlayersSockets.exclude(player: String): Collection<PlayerSocket> {
        return filter { it.player != player }
    }
}
