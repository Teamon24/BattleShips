package org.home.mvc.contoller.server

import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.so
import org.home.net.server.PlayersSockets
import org.home.utils.SocketUtils.isNotClosed

object PlayersSocketsExtensions {
    operator fun PlayersSockets.get(player: String) =
        firstOrNull { it.player == player } ?:
            throw RuntimeException("There is no ${PlayerSocket::class.name} for player \"$player\"")

    fun PlayersSockets.exclude(player: String) = filter { it.player != player }

    inline fun PlayerSocket.isNotClosed(onTrue: PlayerSocket.() -> Unit) = isNotClosed.so { onTrue() }
}



