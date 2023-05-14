package org.home.mvc.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.HasAShot
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.removeFrom
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.yes
import home.extensions.CollectionsExtensions.exclude
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.server.action.HitAction
import org.home.utils.extensions.ObservableValueMap
import org.home.utils.extensions.ObservablePropertiesExtensions.emptySimpleListProperty
import org.home.utils.extensions.ObservablePropertiesExtensions.emptySimpleMapProperty
import org.home.mvc.view.battle.subscriptions.NewServerInfo
import org.home.utils.log
import tornadofx.ViewModel
import tornadofx.onChange
import java.util.concurrent.ConcurrentHashMap

typealias PlayersAndShips = SimpleMapProperty<String, MutableCollection<Ship>>
typealias ShipsTypes = SimpleMapProperty<Int, Int>

class BattleModel : ViewModel {

    val applicationProperties: ApplicationProperties by di()
    val currentPlayer = applicationProperties.currentPlayer

    private val size = applicationProperties.size
    private val maxShipType = applicationProperties.maxShipType
    private val playersNumbers = applicationProperties.playersNumber

    private fun initFleetsReadiness(shipsTypes: Map<Int, Int>): MutableMap<Int, SimpleIntegerProperty> {
        return shipsTypes
            .mapValues { SimpleIntegerProperty(it.value) }
            .toMutableMap()
    }

    inline operator fun BattleModel.invoke(crossinline b: BattleModel.() -> Unit) = this.b()

    private val widthProp: SimpleIntegerProperty
    private val heightProp: SimpleIntegerProperty
    private val playersNumberProp: SimpleIntegerProperty

    val width: SimpleIntegerProperty
    val height: SimpleIntegerProperty
    val playersNumber: SimpleIntegerProperty

    constructor() : super() {
        widthProp = SimpleIntegerProperty(size)
        heightProp = SimpleIntegerProperty(size)
        playersNumberProp = SimpleIntegerProperty(playersNumbers)
        width = bind { widthProp }.apply { onChange { log { "width - $value" } } }
        height = bind { heightProp }.apply { onChange { log { "height - $value" } } }
        playersNumber = bind { playersNumberProp }.apply { onChange { log { "playersNumber - $value" } } }
    }

    private var _newServer: NewServerInfo? = null

    val hasNoServerTransfer get() = _newServer == null

    var newServer: NewServerInfo
        get() = _newServer!!
        set(value) {
            _newServer = value
        }

    fun equalizeSizes() {
        height.value = width.value
    }

    var battleIsEnded = false
        set(value) {
            field = value.yes { battleIsStarted = false }
        }

    var battleIsStarted = false
        set(value) {
            field = value.yes { battleIsEnded = false }
        }

    val battleIsNotStarted get() = !battleIsStarted

    val selectedPlayer = SimpleStringProperty()
    val turn = SimpleStringProperty()

    val fleetsReadiness = ConcurrentHashMap<String, MutableMap<Int, SimpleIntegerProperty>>()

    val shipsTypes =
        emptySimpleMapProperty<Int, Int>()
            .updateFleetReadiness()
            .putInitials()

    val players = emptySimpleListProperty<String>().apply { logOnChange("players") }
    val defeatedPlayers = emptySimpleListProperty<String>().apply { logOnChange("defeated") }

    val readyPlayers = emptySimpleListProperty<String>().apply { logOnChange("ready players") }

    val playersAndShips = emptySimpleMapProperty<String, Ships>()
        .notifyOnChange()
        .putInitials()

    private fun ShipsTypes.updateFleetReadiness(): ShipsTypes {
        addListener(
            MapChangeListener {
                fleetsReadiness.keys.forEach { player ->
                    fleetsReadiness[player] = initFleetsReadiness(shipsTypes)
                }
            })

        return this
    }


    fun getWinner(): String = players.first { it !in defeatedPlayers }

    fun lastButNotDefeated(player: String): Boolean {
        return player !in defeatedPlayers &&
                defeatedPlayers.containsAll(players.exclude(player, currentPlayer))
    }

    private fun SimpleMapProperty<Int, Int>.putInitials(): SimpleMapProperty<Int, Int> {
        (0 until maxShipType).forEach { this[it + 1] = maxShipType - it }
        return this
    }

    @JvmName("putInitialsToPlayersAndShips")
    private fun PlayersAndShips.putInitials(): PlayersAndShips {
        this[currentPlayer] = mutableListOf()
        return this
    }

    private val statistics = mutableListOf<HasAShot>()


    fun getHits(): List<Coord> {
        return getShots { it is HitAction }
    }

    fun getShotsAt(target: String): List<Coord> {
        return getShots { it.target == target }
    }

    private fun getShots(function: (HasAShot) -> Boolean): List<Coord> {
        return statistics.filter(function).map { it.shot }
    }

    private fun PlayersAndShips.notifyOnChange() = apply {
        addListener(
            MapChangeListener { change ->
                val player = change.key
                when {
                    change.wasAdded() -> {
                        players.add(player)
                        setNotReady(player)
                        fleetsReadiness[player] = initFleetsReadiness(shipsTypes)
                        log { "added \"$player\"" }
                    }

                    change.wasRemoved() -> {
                        player {
                            removeFrom(players)
                            removeFrom(readyPlayers)
                            removeFrom(fleetsReadiness)
                            removeFrom(defeatedPlayers)
                            log { "removed \"$this\"" }
                        }

                    }
                }
            })
    }

    fun updateFleetReadiness(event: FleetEditEvent) {
        event {
            fleetsReadiness[player]!![shipType]!!.operation()
        }
    }

    fun putFleetSettings(settings: FleetSettingsAction) {
        width.value = settings.width
        height.value = settings.height
        shipsTypes.clear()
        shipsTypes.putAll(settings.shipsTypes)
    }

    fun addShot(hasAShot: HasAShot) {
        statistics.add(hasAShot)
    }

    fun registersAHit(shot: Coord) = currentPlayer.ships().gotHitBy(shot)

    fun hasNo(target: String, hitCoord: Coord): Boolean {
        return hitCoord !in statistics
            .asSequence()
            .filter { it.target == target }
            .map { it.shot }
    }

    fun String.addedAllShips(): Boolean {
        val decks = this.ships().flatten().count()
        val shouldBe = shipsTypes.entries.sumOf { it.key * it.value }
        log { "hasAllShips [decks == shouldBe; $decks ? $shouldBe"}
        return decks == shouldBe
    }

    inline fun String.addedAllShips(onTrue: () -> Unit) = addedAllShips().so(onTrue)

    fun hasReady(player: String) = player in readyPlayers

    fun setReady(player: String) { readyPlayers.add(player) }
    fun setNotReady(player: String) { readyPlayers.remove(player) }

    fun setReadiness(player: String, ready: Boolean) {
        when {
            ready -> setReady(player)
            else -> setNotReady(player)
        }
    }

    fun hasOnePlayerLeft() = players.size == 1 && battleIsEnded

    fun hasAWinner() = players.size - defeatedPlayers.size == 1
    inline fun hasAWinner(onTrue: () -> Unit) = hasAWinner().so(onTrue)

    inline val String?.isCurrent get() = currentPlayer == this

    fun hasCurrent(player: String?) = player.isCurrent

    inline fun hasCurrent(player: String, onTrue: () -> Unit) = player.isCurrent.so(onTrue)

    inline fun String?.isCurrent(onTrue: () -> Unit) = isCurrent.so(onTrue)

    fun shipsOf(player: String) = playersAndShips[player]!!
    fun String.ships() = playersAndShips[this]!!

    private fun <T> SimpleListProperty<T>.logOnChange(name: String) {
        addListener(ListChangeListener { log { "$name - ${it.list}" } })
    }

    private fun ObservableValueMap<String, Boolean>.logOnChange() {
        addValueListener {
            log { "ready players - ${this@BattleModel.readyPlayers}" }
        }
    }
}

inline val BattleModel.allAreReady get() = readyPlayers.size == playersNumber.value
//SimpleListProperty readyPlayer не сериализуется, поэтому - toMutableList
inline val BattleModel.thoseAreReady get() = readyPlayers.toMutableList()
