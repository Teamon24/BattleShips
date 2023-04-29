package org.home.utils

import org.home.net.PlayerSocket
import tornadofx.Dimension
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

typealias SocketMessages<T, S> = Pair<S, Collection<T>>
typealias SocketsMessages<T, S> = LinkedBlockingQueue<SocketMessages<T, S>>

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
