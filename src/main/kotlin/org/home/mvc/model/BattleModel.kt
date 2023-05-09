package org.home.mvc.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import org.home.mvc.ApplicationProperties
import org.home.net.message.FleetSettingsAction
import org.home.net.message.HasAShot
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.removeFrom
import home.extensions.BooleansExtensions.yes
import home.extensions.CollectionsExtensions.exclude
import home.extensions.ObservablePropertiesExtensions.ObservableValueMap
import home.extensions.ObservablePropertiesExtensions.emptySimpleListProperty
import home.extensions.ObservablePropertiesExtensions.emptySimpleMapProperty
import org.home.utils.log
import tornadofx.ViewModel
import tornadofx.onChange
import java.util.concurrent.ConcurrentHashMap

class BattleModel : ViewModel {

    companion object {
        const val size = 2
        const val shipsTypes = 1
        const val initialPlayersNumber = 3

        fun fleetReadiness(battleShipsTypes: Map<Int, Int>): MutableMap<Int, SimpleIntegerProperty> {
            return battleShipsTypes
                .map { (shipType, number) -> shipType to SimpleIntegerProperty(number) }
                .toMap()
                .toMutableMap()
        }

        inline operator fun BattleModel.invoke(crossinline b: BattleModel.() -> Unit) = this.b()
    }

    private val widthProp: SimpleIntegerProperty
    private val heightProp: SimpleIntegerProperty
    private val playersNumberProp: SimpleIntegerProperty

    val width: SimpleIntegerProperty
    val height: SimpleIntegerProperty
    val playersNumber: SimpleIntegerProperty

    constructor(): super() {
        widthProp = SimpleIntegerProperty(size)
        heightProp = SimpleIntegerProperty(size)
        playersNumberProp = SimpleIntegerProperty(initialPlayersNumber)
        width = bind { widthProp }.apply { onChange { log { "width - $value" } } }
        height = bind { heightProp }.apply { onChange { log { "height - $value" } } }
        playersNumber = bind { playersNumberProp }.apply { onChange { log { "playersNumber - $value" } } }
    }

    constructor(model: BattleModel): super() {
        widthProp = SimpleIntegerProperty(model.width.value)
        heightProp = SimpleIntegerProperty(model.height.value)
        playersNumberProp = SimpleIntegerProperty(model.playersNumber.value)
        battleShipsTypes.putAll(model.battleShipsTypes)
        width = bind { widthProp }.apply { onChange { log { "width - $value" } } }
        height = bind { heightProp }.apply { onChange { log { "height - $value" } } }
        playersNumber = bind { playersNumberProp }.apply { onChange { log { "playersNumber - $value" } } }
    }

    lateinit var newServer: Pair<String, Int>
    val applicationProperties: ApplicationProperties by di()

    val currentPlayer = applicationProperties.currentPlayer

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

    val battleShipsTypes =
        emptySimpleMapProperty<Int, Int>()
            .putInitials()
            .updateFleetReadiness()

    val players = emptySimpleListProperty<String>().apply { log("players") }
    val defeatedPlayers = emptySimpleListProperty<String>().apply { log("defeated") }

    val fleetsReadiness = ConcurrentHashMap<String, MutableMap<Int, SimpleIntegerProperty>>()
    val playersReadiness = ObservableValueMap<String, Boolean>()

    val playersAndShips = emptySimpleMapProperty<String, MutableList<Ship>>()
        .notifyOnChange()
        .putInitials()

    private fun SimpleMapProperty<Int, Int>.updateFleetReadiness(): SimpleMapProperty<Int, Int> {
        addListener(
            MapChangeListener {
                fleetsReadiness.keys.forEach { player ->
                    fleetsReadiness[player] = fleetReadiness(battleShipsTypes)
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
        (0 until shipsTypes).forEach { this[it + 1] = shipsTypes - it }
        return this
    }

    @JvmName("putInitialsToPlayersAndShips")
    private fun SimpleMapProperty<String, MutableList<Ship>>.putInitials(): SimpleMapProperty<String, MutableList<Ship>> {
        this[currentPlayer] = mutableListOf()
        return this
    }

    private val statistics = mutableListOf<HasAShot>()


    fun getShots(player: String): List<Coord> {
        return statistics.filter { it.target == player }.map { it.shot }
    }

    private fun SimpleMapProperty<String, MutableList<Ship>>.notifyOnChange() = apply {
        addListener(
            MapChangeListener { change ->
                val player = change.key
                when {
                    change.wasAdded() -> {
                        players.add(player)
                        setNotReady(player)
                        fleetsReadiness[player] = fleetReadiness(battleShipsTypes)
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

    fun putSettings(settings: FleetSettingsAction) {
        width.value = settings.width
        height.value = settings.height
        battleShipsTypes.clear()
        battleShipsTypes.putAll(settings.shipsTypes)
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

    fun hasAllShips(player: String): Boolean {
        val decks = shipsOf(player).flatten().count()
        val shouldBe = battleShipsTypes.entries.sumOf { it.key * it.value }
        log { "hasAllShips [decks == shouldBe; $decks ? $shouldBe"}
        return decks == shouldBe
    }

    fun hasReady(player: String) = playersReadiness[player]!!

    fun setReady(player: String) { playersReadiness[player] = true }
    fun setReady(player: String, ready: Boolean) { playersReadiness[player] = ready }
    fun setNotReady(player: String) { playersReadiness[player] = false }
    fun hasOnePlayerLeft() = players.size == 1 && battleIsEnded
    fun hasAWinner() = players.size - defeatedPlayers.size == 1
    fun hasNoWinner() = players.size - defeatedPlayers.size > 1
    fun currentPlayerIs(player: String) = currentPlayer == player
    fun shipsOf(player: String) = playersAndShips[player]!!

    private fun <T> SimpleListProperty<T>.log(name: String) {
        addListener(ListChangeListener {
            log { "$name - ${it.list}" }
        })
    }
}

val BattleModel.notAllReady get() =
    playersReadiness.any(::isNotReady) || (playersReadiness.size != playersNumber.value)

val BattleModel.allAreReady get() = playersReadiness.all(::isReady) && playersReadiness.size == playersNumber.value
val BattleModel.thoseAreReady get() = playersReadiness.filter(::isReady).keys.toSet()

private fun isReady(it: Map.Entry<String, Boolean>) = it.value
private fun isNotReady(it: Map.Entry<String, Boolean>) = !it.value

