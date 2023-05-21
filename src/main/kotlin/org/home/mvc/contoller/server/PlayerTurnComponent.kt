package org.home.mvc.contoller.server

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.thus
import home.extensions.CollectionsExtensions.hasElements
import org.home.mvc.GameComponent
import org.home.utils.log

class PlayerTurnComponent: GameComponent() {

    private val backingTurnList = mutableListOf<String>()
    val turnList: List<String> get() = backingTurnList

    var turnPlayer: String? = null

    inline val String.hasATurn get() = this == turnPlayer
    inline val <E> Collection<E>.hasPlayers get() = hasElements

    fun startTurn(): String {
        backingTurnList {
            clear()
            addAll(modelView.getPlayers().shuffled())
            log { "turn $this" }
            turnPlayer = first()
        }
        return turnPlayer!!
    }

    fun nextTurnAndRemove(player: String): String {
        val nextTurn = nextTurn()
        remove(player)
        return nextTurn
    }

    fun nextTurn(): String {
        log { "previous turn: $turnPlayer" }
        var nextTurnIndex = turnList.indexOf(turnPlayer) + 1
        if (nextTurnIndex > turnList.size - 1) {
            nextTurnIndex = 0
        }
        turnPlayer = turnList[nextTurnIndex]
        log { "next turn: $turnPlayer" }
        return turnPlayer!!
    }

    fun remove(removedPlayer: String) = backingTurnList.remove(removedPlayer)
    inline fun hasPlayers(onTrue: () -> Unit) = turnList.hasPlayers(onTrue)
    inline fun battleIsStarted(onTrue: () -> Unit) = (turnPlayer != null).thus(onTrue)
    inline fun hasATurn(player: String, onTrue: () -> Unit) = player.hasATurn.thus(onTrue)
}