package org.home.mvc.model

import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.exclude
import home.extensions.CollectionsExtensions.hasElements
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import org.home.mvc.GameViewModel
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.view.battle.subscription.NewServerInfo

abstract class BattleViewModel: GameViewModel() {
    abstract fun getWidth(): SimpleIntegerProperty
    abstract fun getHeight(): SimpleIntegerProperty
    abstract fun getPlayersNumber(): SimpleIntegerProperty
    abstract fun getNewServer(): NewServerInfo
    abstract fun getFleetsReadiness(): FleetsReadiness
    abstract fun getShipsTypes(): ShipsTypes
    abstract fun getPlayers(): SimpleListProperty<String>
    abstract fun getEnemies(): SimpleListProperty<String>
    abstract fun getDefeatedPlayers(): SimpleListProperty<String>
    abstract fun getReadyPlayers(): SimpleSetProperty<String>
    abstract fun getShotsAt(target: String): Collection<Coord>
    abstract fun getShots(function: (HasAShot) -> Boolean): Collection<Coord>
    abstract fun getShipBy(hasAShot: HasAShot): MutableList<Coord>
    abstract fun addShot(hasAShot: HasAShot)
    abstract fun shipsOf(player: String): Ships
    abstract fun String.hasDeck(deck: Coord): Boolean
    abstract fun String.decks(): List<Coord>
    abstract fun String.ships(): Ships
    abstract fun battleIsEnded(value: Boolean)
    abstract fun battleIsStarted(value: Boolean)
    abstract fun battleIsEnded(): Boolean
    abstract fun battleIsStarted(): Boolean
    abstract fun battleIsNotStarted(): Boolean
    abstract fun equalizeSizes()
    abstract fun copyShipsTypes(): ShipsTypes
    abstract val turn: SimpleStringProperty
    abstract fun fleetReadiness(player: String): MutableMap<Int, SimpleIntegerProperty>
    abstract fun getHits(): Collection<Coord>
    abstract fun hasNo(target: String, hitCoord: Coord): Boolean
    abstract fun getCurrentPlayer(): String
    abstract fun updateFleetReadiness(event: FleetEditEvent)
    abstract fun putFleetSettings(settings: FleetSettingsAction)
    abstract fun hasNoServerTransfer(): Boolean
    abstract fun add(player: String)
    abstract fun remove(player: String)
    abstract fun setNewServer(newServerInfo: NewServerInfo)

    fun newServer(init: NewServerInfo.() -> Unit) {
        setNewServer(NewServerInfo().apply(init))
    }

    fun battleIsStarted(onTrue: () -> Unit) = battleIsStarted().so(onTrue)
    fun exclude(player: String) = getPlayers().exclude(player)
    fun noPropertyFleetReadiness(player: String) = fleetReadiness(player).mapValues { it.value.value }
    fun noPropertyFleetReadiness() = getFleetsReadiness().mapValues { it.value.mapValues { it.value.value } }
    fun registersAHit(shot: Coord) = getCurrentPlayer().ships().gotHitBy(shot)

    fun hasReady(player: String): Boolean  = player in getReadyPlayers()
    inline fun hasReady(player: String, onTrue: () -> Unit) = hasReady(player).so(onTrue)
    fun setReady(player: String)  { getReadyPlayers().add(player) }
    fun setAllReady(readyPlayers: Collection<String>)  { getReadyPlayers().addAll(readyPlayers) }
    fun setNotReady(player: String)  { getReadyPlayers().remove(player) }

    inline fun hasEnemies() = getEnemies().hasElements
    inline val String?.isCurrent get() = getCurrentPlayer() == this
    inline val String?.isNotCurrent get() = getCurrentPlayer() != this
    inline fun String?.isCurrent(onTrue: () -> Unit) = isCurrent.so(onTrue)
    inline fun String?.isNotCurrent(onTrue: () -> Unit) = isCurrent.otherwise(onTrue)
    inline fun hasCurrent(player: String?) = player.isCurrent

    fun getWinner(): String = getPlayers().first { it !in getDefeatedPlayers() }
    fun hasOnePlayerLeft() = getPlayers().size == 1 && battleIsEnded()
    fun hasAWinner() = getPlayers().size - getDefeatedPlayers().size == 1
    inline fun hasAWinner(onTrue: () -> Unit) = hasAWinner().so(onTrue)

    fun lastShipWasAdded(player: String, onTrue: () -> Unit) = player.lastShipWasEdited(0).so(onTrue)
    fun lastShipWasDeleted(player: String, onTrue: () -> Unit) = player.lastShipWasEdited(1).so(onTrue)
    private fun String.lastShipWasEdited(i: Int): Boolean {
        val restShipsNumbers = getFleetsReadiness()[getCurrentPlayer()]!!
            .values
            .map { it.value }

        return restShipsNumbers.run {
            val otherTypesAdded = count { it == 0 } == size - i
            val restTypeEdited = count { it == 1 } == i
            otherTypesAdded && restTypeEdited
        }
    }

    fun getShipsNumber(type: Int) = getShipsTypes()[type]!!
    fun lastShipType() = getShipsTypes().maxOfOrNull { entry -> entry.key } ?: 0

}

inline operator fun BattleViewModel.invoke(crossinline b: BattleViewModel.() -> Unit) = this.b()