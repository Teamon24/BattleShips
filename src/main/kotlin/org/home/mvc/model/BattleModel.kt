package org.home.mvc.model

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.removeFrom
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.yes
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import org.home.app.ApplicationProperties
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.view.battle.subscription.NewServerInfo
import org.home.utils.extensions.ObservablePropertiesExtensions.copy
import org.home.utils.extensions.ObservablePropertiesExtensions.emptySimpleListProperty
import org.home.utils.extensions.ObservablePropertiesExtensions.emptySimpleMapProperty
import org.home.utils.extensions.onMapChange
import org.home.utils.log
import tornadofx.ViewModel
import tornadofx.onChange
import java.util.concurrent.ConcurrentHashMap

typealias PlayersAndShips = SimpleMapProperty<String, Ships>
typealias ShipsTypes = SimpleMapProperty<Int, Int>
private typealias FleetsReadiness = ConcurrentHashMap<String, MutableMap<Int, SimpleIntegerProperty>>
typealias FleetReadiness = MutableMap<Int, SimpleIntegerProperty>

class BattleModel : ViewModel() {

    val applicationProperties: ApplicationProperties by di()
    val currentPlayer = applicationProperties.currentPlayer

    private val size = applicationProperties.size
    private var maxShipType = applicationProperties.maxShipType

    inline operator fun BattleModel.invoke(crossinline b: BattleModel.() -> Unit) = this.b()

    private val widthProp = SimpleIntegerProperty(size)
    private val heightProp = SimpleIntegerProperty(size)
    private val playersNumberProp = SimpleIntegerProperty(applicationProperties.playersNumber)

    val width = bind { widthProp }.apply { onChange { log { "width - $value" } } }
    val height = bind { heightProp }.apply { onChange { log { "height - $value" } } }
    val playersNumber = bind { playersNumberProp }.apply { onChange { log { "playersNumber - $value" } } }

    private var _newServer: NewServerInfo? = null

    val hasNoServerTransfer get() = _newServer == null

    var newServer: NewServerInfo
        get() = _newServer!!
        set(value) { _newServer = value }

    fun equalizeSizes() {
        height.value = width.value
    }

    var battleIsEnded = false
        set(value) { field = value.yes { battleIsStarted = false } }

    var battleIsStarted = false
        set(value) { field = value.yes { battleIsEnded = false } }

    val battleIsNotStarted get() = !battleIsStarted

    val turn = SimpleStringProperty()

    //SHIPS TYPES
    val fleetsReadiness = FleetsReadiness()

    val shipsTypes = emptySimpleMapProperty<Int, Int>()
        .updateFleetReadiness()
        .putInitials()

    fun copyShipsTypes(): ShipsTypes {
        val copy = shipsTypes.copy()
        applicationProperties.ships?.let { ships ->
            ships.forEach {
                copy[it.size] = copy[it.size]!! - 1
            }
        }
        return copy
    }

    //PLAYERS
    val players = emptySimpleListProperty<String>().logOnChange("players")
    val enemies = emptySimpleListProperty<String>().logOnChange("enemies")
    val defeatedPlayers = emptySimpleListProperty<String>().logOnChange("defeated")
    val readyPlayers = emptySimpleListProperty<String>().logOnChange("ready players")
    private val statistics = mutableListOf<HasAShot>()

    private fun initFleetsReadiness(shipsTypes: Map<Int, Int>): FleetReadiness {
        return shipsTypes
            .mapValues { SimpleIntegerProperty(it.value) }
            .toMutableMap()
    }

    private val playersAndShips = emptySimpleMapProperty<String, Ships>()
        .notifyOnChange()
        .putInitials()

    private fun PlayersAndShips.notifyOnChange() = apply {
        onMapChange { change ->
            val player = change.key
            when {
                change.wasAdded() -> {
                    player.also {
                        players.add(it)
                        it.isNotCurrent { enemies.add(it) }
                        setNotReady(it)
                        fleetsReadiness[it] = initFleetsReadiness(shipsTypes)
                        log { "added \"$it\"" }
                    }
                }

                change.wasRemoved() -> {
                    player {
                        removeFrom(players)
                        isNotCurrent { removeFrom(enemies) }
                        removeFrom(readyPlayers)
                        removeFrom(fleetsReadiness)
                        removeFrom(defeatedPlayers)
                        log { "removed \"${this@player}\"" }
                    }
                }
            }
        }
    }

    private fun SimpleMapProperty<Int, Int>.putInitials() = apply {
        0.until(maxShipType).forEach {
            put(it + 1, maxShipType - it)
        }
    }

    private fun ShipsTypes.updateFleetReadiness() = apply {
        onMapChange {
            fleetsReadiness {
                keys.forEach { player ->
                    put(player, initFleetsReadiness(this@apply))
                }
            }
        }
    }

    @JvmName("putInitialsToPlayersAndShips")
    private fun PlayersAndShips.putInitials(): PlayersAndShips {
        applicationProperties.ships?.let { ships ->
            set(currentPlayer, ships)
            log { "init ships from app props - $ships" }
            fleetsReadiness {
                val initialFleetReadiness = ships.fleetReadiness()
                if (initialFleetReadiness == shipsTypes) {
                    setReady(currentPlayer)
                }
                put(currentPlayer, initFleetsReadiness(initialFleetReadiness))
                ships.forEach { ship ->
                    update(ShipWasAdded(currentPlayer, ship.size))
                }
            }
        } ?: run {
            set(currentPlayer, mutableListOf())
        }

        return this
    }

    fun FleetsReadiness.update(event: FleetEditEvent) {
        event {
            get(player)!![shipType]!!.propOp()
        }
    }

    fun putFleetSettings(settings: FleetSettingsAction) {
        width.value = settings.width
        height.value = settings.height
        shipsTypes.clear()
        shipsTypes.putAll(settings.shipsTypes)
    }

    fun add(player: String) {
        playersAndShips[player] = mutableListOf()
    }

    fun remove(player: String) {
        playersAndShips.remove(player)
        playersNumber.value = playersNumber.value - 1
    }

    //-------------------------------------------------------------------------------------------------
    //Winner
    fun getWinner(): String = players.first { it !in defeatedPlayers }
    fun hasOnePlayerLeft() = players.size == 1 && battleIsEnded
    fun hasAWinner() = players.size - defeatedPlayers.size == 1
    inline fun hasAWinner(onTrue: () -> Unit) = hasAWinner().so(onTrue)

    //-------------------------------------------------------------------------------------------------
    //Ready
    fun hasReady(player: String) = player in readyPlayers
    fun setReady(player: String) { readyPlayers.add(player) }
    fun setNotReady(player: String) { readyPlayers.remove(player) }
    inline fun hasReady(player: String, onTrue: () -> Unit) = hasReady(player).so(onTrue)

    //-------------------------------------------------------------------------------------------------
    //Shots
    fun getHits() = getShots { it is HitAction }
    fun getShotsAt(target: String) = getShots { it.target == target }
    fun addShot(hasAShot: HasAShot) { statistics.add(hasAShot) }
    private fun getShots(function: (HasAShot) -> Boolean) = statistics.filter(function).map { it.shot }

    fun registersAHit(shot: Coord) = currentPlayer.ships().gotHitBy(shot)

    fun hasNo(target: String, hitCoord: Coord): Boolean {
        return hitCoord !in statistics
            .asSequence()
            .filter { it.target == target }
            .map { it.shot }
    }
    //-------------------------------------------------------------------------------------------------
    //isCurrent
    inline val String?.isCurrent get() = currentPlayer == this
    inline val String?.isNotCurrent get() = currentPlayer != this
    inline fun String?.isCurrent(onTrue: () -> Unit) = isCurrent.so(onTrue)
    private inline fun String?.isNotCurrent(onTrue: () -> Unit) = isCurrent.otherwise(onTrue)
    fun hasCurrent(player: String?) = player.isCurrent
    //-------------------------------------------------------------------------------------------------
    //Ships
    fun shipsOf(player: String): Ships = playersAndShips[player]!!
    fun String.ships() = playersAndShips[this]!!
    fun String.decks() = playersAndShips[this]!!.flatten()
    fun String.hasDeck(deck: Coord) = playersAndShips[this]!!.flatten().contains(deck)

    fun getShipBy(hasAShot: HasAShot): MutableList<Coord> {
        return mutableListOf<Coord>().also {
            getRightNextTo(hasAShot.shot, it)
        }
    }

    private fun getRightNextTo(
        coord: Coord,
        container: MutableList<Coord>
    ) {
        var tempCont = getHits().getRightNextTo(coord)
        if (container.containsAll(tempCont)) return
        container.addAll(tempCont)

        tempCont.forEach {
            getRightNextTo(it, container)
        }
    }

    fun lastShipWasAdded(player: String, onTrue: () -> Unit) = player.lastShipWasEdited(0).so(onTrue)
    fun lastShipWasDeleted(player: String, onTrue: () -> Unit) = player.lastShipWasEdited(1).so(onTrue)

    private fun String.lastShipWasEdited(i: Int): Boolean {
        val restShipsNumbers = fleetsReadiness[currentPlayer]!!
            .values
            .map { it.value }

        return restShipsNumbers.run {
            val otherTypesAdded = count { it == 0 } == size - i
            val restTypeEdited = count { it == 1 } == i
            otherTypesAdded && restTypeEdited
        }
    }

    private fun <T> SimpleListProperty<T>.logOnChange(name: String) = apply {
        addListener(ListChangeListener { log { "$name - ${it.list}" } })
    }
}

inline val BattleModel.allAreReady get() = readyPlayers.size == playersNumber.value
//SimpleListProperty readyPlayer не сериализуется, поэтому - toMutableList
inline val BattleModel.thoseAreReady get() = readyPlayers.toMutableList()
