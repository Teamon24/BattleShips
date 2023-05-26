package org.home.mvc.model

import home.extensions.AnysExtensions.addTo
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.removeFrom
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import home.extensions.CollectionsExtensions.exclude
import home.extensions.CollectionsExtensions.hasElements
import home.extensions.CollectionsExtensions.isNotEmpty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import org.home.mvc.GameViewModel
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.view.battle.subscription.NewServerInfo
import org.home.utils.extensions.onMapChange
import org.home.utils.log

abstract class BattleViewModel: GameViewModel() {
    abstract fun getWidth(): SimpleIntegerProperty
    abstract fun getHeight(): SimpleIntegerProperty
    abstract fun getPlayersNumber(): SimpleIntegerProperty
    abstract fun getPlayersAndShipsNumber(): PlayersAndShips
    abstract fun getNewServerInfo(): NewServerInfo
    abstract fun getFleetsReadiness(): FleetsReadiness
    abstract fun setFleetReadiness(player: String, readiness: FleetReadiness)
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
    abstract fun getFleetReadiness(player: String): MutableMap<Int, SimpleIntegerProperty>
    abstract fun getHits(): Collection<Coord>
    abstract fun hasNo(target: String, hitCoord: Coord): Boolean
    abstract fun getCurrentPlayer(): String
    abstract fun updateFleetReadiness(event: FleetEditEvent)
    abstract fun putFleetSettings(settings: FleetSettingsAction)
    abstract fun hasNoServerTransfer(): Boolean
    abstract fun add(player: String)
    abstract fun remove(player: String)

    protected abstract fun newServerInfo(newServerInfo: NewServerInfo)
    fun newServer(init: NewServerInfo.() -> Unit) { setNewServer(NewServerInfo().apply(init)) }
    fun setNewServer(newServerInfo: NewServerInfo) {
        newServerInfo {
            newServerInfo(this)
            readyPlayers.isNotEmpty {
                getReadyPlayers().clear()
                getReadyPlayers().addAll(readyPlayers)
            }
        }
    }

    fun battleIsStarted(onTrue: () -> Unit)      = battleIsStarted().so(onTrue)
    fun exclude(player: String)                  = getPlayers().exclude(player)
    fun noPropertyFleetReadiness(player: String) = propless(getFleetReadiness(player))
    fun noPropertyFleetReadiness()               = getFleetsReadiness().mapValues { propless(it.value) }
    fun registersAHit(shot: Coord)               = getCurrentPlayer().ships().gotHitBy(shot)


    inline fun hasReady(player: String, onTrue: () -> Unit) = hasReady(player).so(onTrue)
    fun hasReady(player: String): Boolean                   = player in getReadyPlayers()

    @JvmName("noReceiverSetReady")
    fun setReady(player: String)                      { getReadyPlayers().add(player) }
    @JvmName("noReceiverSetNotReady")
    fun setNotReady(player: String)                   { getReadyPlayers().remove(player) }
    fun setAllReady(readyPlayers: Collection<String>) { getReadyPlayers().addAll(readyPlayers) }

    fun String.setReady()                             { getReadyPlayers().add(this) }
    fun String.setNotReady()                          { getReadyPlayers().remove(this) }



    inline fun hasEnemies()                             = getEnemies().hasElements
    inline val String?.isCurrent get()                  = getCurrentPlayer() == this
    inline val String?.isNotCurrent get()               = getCurrentPlayer() != this
    inline fun String?.isCurrent(onTrue: () -> Unit)    = isCurrent.apply               {so(onTrue) }
    inline fun String?.isNotCurrent(onTrue: () -> Unit) = isCurrent.otherwise(onTrue)
    inline fun hasCurrent(player: String?)              = player.isCurrent


    fun getWinner()                                            = getPlayers().first { it !in getDefeatedPlayers() }
    fun hasOnePlayerLeft()                                     = getPlayers().size == 1 && battleIsEnded()
    fun hasAWinner()                                           = getPlayers().size - getDefeatedPlayers().size == 1
    inline fun hasAWinner(onTrue: () -> Unit)                  = hasAWinner().so(onTrue)

    fun lastShipWasAdded(player: String, onTrue: () -> Unit)   = player.lastShipWasEdited(0).so(onTrue)
    fun lastShipWasDeleted(player: String, onTrue: () -> Unit) = player.lastShipWasEdited(1).so(onTrue)
    fun getShipsNumber(type: Int)                              = getShipsTypes()[type]!!
    fun lastShipType() = getShipsTypes().maxOfOrNull { entry -> entry.key } ?: 0

    fun hasDefeated(player: String) = player in getDefeatedPlayers()
    val String.isDefeated get()     = this in getDefeatedPlayers()

    private fun propless(mutableMap: MutableMap<Int, SimpleIntegerProperty>) = mutableMap.mapValues { it.value.value }

    protected fun PlayersAndShips.addOnChange() = apply {
        onMapChange { change ->
            val player = change.key
            when {
                change.wasAdded() -> {
                    player {
                        addTo(getPlayers())
                        isNotCurrent { addTo(getEnemies()) }
                        setNotReady()
                        setFleetReadiness(this, getShipsTypes().toFleetsReadiness())
                    }
                }

                change.wasRemoved() -> {
                    player {
                        removeFrom(getPlayers())
                        isNotCurrent { removeFrom(getEnemies()) }
                        removeFrom(getReadyPlayers())
                        removeFrom(getFleetsReadiness())
                        removeFrom(getDefeatedPlayers())
                    }
                }
            }

            log { "${change.wasAdded().then("added").or("removed")} \"$player\"" }
        }
    }

    fun ShipsTypes.updateFleetReadiness() = apply {
        onMapChange {
            getFleetsReadiness().apply {
                keys.forEach { player -> put(player, toFleetsReadiness()) }
            }
        }
    }

    fun Map<Int, Int>.toFleetsReadiness(): FleetReadiness =
        mapValues { SimpleIntegerProperty(it.value) }.toMutableMap()

    private fun String.lastShipWasEdited(i: Int): Boolean {
        val restShipsNumbers = getFleetsReadiness()[this]!!.values.map { it.value }

        return restShipsNumbers.run {
            val otherTypesAdded = count { it == 0 } == size - i
            val restTypeEdited = count { it == 1 } == i
            otherTypesAdded && restTypeEdited
        }
    }
}

inline operator fun BattleViewModel.invoke(crossinline b: BattleViewModel.() -> Unit) = this.b()
