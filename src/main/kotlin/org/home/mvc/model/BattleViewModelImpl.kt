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
import tornadofx.onChange
import java.util.concurrent.ConcurrentHashMap

typealias PlayersAndShips = SimpleMapProperty<String, Ships>
typealias ShipsTypes = SimpleMapProperty<Int, Int>
typealias FleetsReadiness = ConcurrentHashMap<String, MutableMap<Int, SimpleIntegerProperty>>
typealias FleetReadiness = MutableMap<Int, SimpleIntegerProperty>

class BattleViewModelImpl : BattleViewModel() {
    val applicationProperties: ApplicationProperties by di()

    private val _currentPlayer = applicationProperties.currentPlayer
    private val size = applicationProperties.size

    private var maxShipType = applicationProperties.maxShipType

    private val widthProp = SimpleIntegerProperty(size)
    private val heightProp = SimpleIntegerProperty(size)

    private val playersNumberProp = SimpleIntegerProperty(applicationProperties.playersNumber)
    private val width = bind { widthProp }.apply { onChange { log { "width - $value" } } }
    private val height = bind { heightProp }.apply { onChange { log { "height - $value" } } }
    private val playersNumber = bind { playersNumberProp }.apply { onChange { log { "playersNumber - $value" } } }
    private val shipsTypes = emptySimpleMapProperty<Int, Int>().updateFleetReadiness().putInitials()
    private val fleetsReadiness = FleetsReadiness()

    private val players = emptySimpleListProperty<String>().logOnChange("players")
    private val enemies = emptySimpleListProperty<String>().logOnChange("enemies")
    private val defeatedPlayers = emptySimpleListProperty<String>().logOnChange("defeated")
    private val readyPlayers = emptySimpleListProperty<String>().logOnChange("ready players")

    private var battleIsEnded = false
    private var battleIsStarted = false
    private var newServer: NewServerInfo? = null


    override fun add(player: String) { playersAndShips[player] = mutableListOf() }

    override fun remove(player: String) {
        playersAndShips.remove(player)
        getPlayersNumber().value = getPlayersNumber().value - 1
    }

    override fun getWidth(): SimpleIntegerProperty = width
    override fun getHeight(): SimpleIntegerProperty = height
    override fun getPlayersNumber(): SimpleIntegerProperty = playersNumber

    override fun getNewServer() = newServer!!
    override fun setNewServer(value: NewServerInfo) { newServer = value }

    override fun equalizeSizes() { getHeight().value = getWidth().value }

    override fun battleIsEnded(value: Boolean) { battleIsEnded = value.yes { battleIsStarted = false } }
    override fun battleIsStarted(value: Boolean) { battleIsEnded = value.yes { battleIsEnded = false } }
    override fun battleIsEnded() = battleIsEnded
    override fun battleIsStarted() = battleIsStarted
    override fun battleIsNotStarted() = !battleIsStarted
    override val turn = SimpleStringProperty()

    //-------------------------------------------------------------------------------------------------
    //Fleets readiness
    override fun fleetReadiness(player: String) = fleetsReadiness[player]!!
    override fun getFleetsReadiness() = fleetsReadiness

    override fun updateFleetReadiness(event: FleetEditEvent) {
        event {
            fleetsReadiness[player]!![shipType]!!.propOp()
        }
    }

    override fun putFleetSettings(settings: FleetSettingsAction) {
        getWidth().value = settings.width
        getHeight().value = settings.height
        shipsTypes.clear()
        shipsTypes.putAll(settings.shipsTypes)
    }

    override fun hasNoServerTransfer() = newServer == null

    //-------------------------------------------------------------------------------------------------
    //players
    override fun getCurrentPlayer() = _currentPlayer
    override fun getPlayers(): SimpleListProperty<String> = players
    override fun getEnemies(): SimpleListProperty<String> = enemies
    override fun getDefeatedPlayers(): SimpleListProperty<String> = defeatedPlayers
    override fun getReadyPlayers(): SimpleListProperty<String> = readyPlayers

    //-------------------------------------------------------------------------------------------------
    //shots
    override fun getHits() = getShots { it is HitAction }
    override fun getShotsAt(target: String) = getShots { it.target == target }
    override fun addShot(hasAShot: HasAShot) { statistics.add(hasAShot) }

    override fun getShots(function: (HasAShot) -> Boolean): Collection<Coord> =
        statistics.filter(function).map { it.shot }

    override fun hasNo(target: String, hitCoord: Coord): Boolean {
        return hitCoord !in statistics
            .asSequence()
            .filter { it.target == target }
            .map { it.shot }
    }

    //-------------------------------------------------------------------------------------------------
    //ships
    override fun String.ships() = playersAndShips[this]!!
    override fun String.decks() = playersAndShips[this]!!.flatten()
    override fun String.hasDeck(deck: Coord) = playersAndShips[this]!!.flatten().contains(deck)
    override fun shipsOf(player: String): Ships = playersAndShips[player]!!

    override fun getShipBy(hasAShot: HasAShot) = mutableListOf<Coord>().also { getRightNextTo(hasAShot.shot, it) }
    override fun getShipsTypes() = shipsTypes


    override fun copyShipsTypes(): ShipsTypes {
        val copy = shipsTypes.copy()
        applicationProperties.ships?.let { ships ->
            ships.forEach {
                copy[it.size] = copy[it.size]!! - 1
            }
        }
        return copy
    }

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
            set(_currentPlayer, ships)
            log { "init ships from app props - $ships" }
            fleetsReadiness {
                val initialFleetReadiness = ships.fleetReadiness()
                if (initialFleetReadiness == shipsTypes) {
                    setReady(_currentPlayer)
                }
                put(_currentPlayer, initFleetsReadiness(initialFleetReadiness))
                ships.forEach { ship ->
                    updateFleetReadiness(ShipWasAdded(_currentPlayer, ship.size))
                }
            }
        } ?: run {
            set(_currentPlayer, mutableListOf())
        }

        return this
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

    private fun <T> SimpleListProperty<T>.logOnChange(name: String) = apply {
        addListener(ListChangeListener { log { "$name - ${it.list}" } })
    }
}

inline val BattleViewModel.allAreReady get() = getReadyPlayers().size == getPlayersNumber().value
//SimpleListProperty readyPlayer не сериализуется, поэтому - toMutableList
inline val BattleViewModel.thoseAreReady get() = getReadyPlayers().toMutableList()
