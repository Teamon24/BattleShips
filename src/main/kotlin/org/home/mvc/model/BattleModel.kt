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

    val playersReadiness = ObservableValueMap<String, Boolean>().apply { logOnChange() }

    val playersAndShips = emptySimpleMapProperty<String, MutableCollection<Ship>>()
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


    fun getShots(player: String): List<Coord> {
        return statistics.filter { it.target == player }.map { it.shot }
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
                            removeFrom(playersReadiness)
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

    fun registersAHit(shot: Coord): Boolean {
        return shipsOf(currentPlayer).areHit(shot)
    }

    fun hasNo(enemyToHit: String, hitCoord: Coord): Boolean {
        return hitCoord !in statistics
            .asSequence()
            .filter { it.player == currentPlayer }
            .filter { it.target == enemyToHit }
            .map { it.shot }
    }

    fun String.createdAllShips(): Boolean {
        val decks = shipsOf(this).flatten().count()
        val shouldBe = shipsTypes.entries.sumOf { it.key * it.value }
        log { "hasAllShips [decks == shouldBe; $decks ? $shouldBe"}
        return decks == shouldBe
    }

    fun hasReady(player: String) = playersReadiness[player]!!

    fun setReady(player: String) { playersReadiness[player] = true }
    fun setNotReady(player: String) { playersReadiness[player] = false }
    fun setReadiness(player: String, ready: Boolean) { playersReadiness[player] = ready }
    fun hasOnePlayerLeft() = players.size == 1 && battleIsEnded

    fun hasAWinner() = players.size - defeatedPlayers.size == 1
    inline fun hasAWinner(onTrue: () -> Unit) = hasAWinner().so(onTrue)

    inline val String?.isCurrent get() = currentPlayer == this

    fun hasCurrent(player: String?) = player.isCurrent

    inline fun hasCurrent(player: String, onTrue: () -> Unit) = player.isCurrent.so(onTrue)

    inline fun String?.isCurrent(onTrue: () -> Unit) = isCurrent.so(onTrue)

    fun shipsOf(player: String) = playersAndShips[player]!!

    private fun <T> SimpleListProperty<T>.logOnChange(name: String) {
        addListener(ListChangeListener {
            log { "$name - ${it.list}" }
        })
    }

    private fun ObservableValueMap<String, Boolean>.logOnChange() {
        addValueListener {
            log { "playersReadiness: ${this@BattleModel.playersReadiness}" }
        }
    }
}

val BattleModel.notAllReady get() =
    playersReadiness.any(::isNotReady) || (playersReadiness.size != playersNumber.value)

val BattleModel.allAreReady get() = playersReadiness.all(::isReady) && playersReadiness.size == playersNumber.value
val BattleModel.thoseAreReady get() = playersReadiness.filter(::isReady).keys.toSet()

private fun isReady(it: Map.Entry<String, Boolean>) = it.value
private fun isNotReady(it: Map.Entry<String, Boolean>) = !it.value

