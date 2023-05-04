package org.home.mvc.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import org.home.mvc.ApplicationProperties
import org.home.net.action.FleetSettingsAction
import org.home.net.action.HasAShot
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.removeFrom
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.extensions.ObservablePropertiesExtensions.ObservableValueMap
import org.home.utils.extensions.ObservablePropertiesExtensions.emptySimpleListProperty
import org.home.utils.extensions.ObservablePropertiesExtensions.emptySimpleMapProperty
import org.home.utils.log
import tornadofx.ViewModel

class BattleModel : ViewModel() {

    lateinit var newServer: Pair<String, Int>
    private val appProps: ApplicationProperties by di()
    val currentPlayer = appProps.currentPlayer

    companion object {
        const val size = 5
        const val shipsTypes = 4
        const val initialPlayersNumber = 3

        fun fleetReadiness(battleShipsTypes: Map<Int, Int>): MutableMap<Int, SimpleIntegerProperty> {
            return battleShipsTypes
                .map { (shipType, number) -> shipType to SimpleIntegerProperty(number) }
                .toMap()
                .toMutableMap()
        }

        inline operator fun BattleModel.invoke(crossinline b: BattleModel.() -> Unit) = this.b()
    }

    fun equalizeSizes() {
        height.value = width.value
    }

    var battleIsEnded = false
        set(value) {
            value.so { battleIsStarted = false }
            field = value
        }

    var battleIsStarted = false
        set(value) {
            value.so { battleIsEnded = false }
            field = value
        }

    private val widthProp = SimpleIntegerProperty(size)
    private val heightProp = SimpleIntegerProperty(size)

    private val playersNumberProp = SimpleIntegerProperty(initialPlayersNumber)

    val width = bind { widthProp }
    val height = bind { heightProp }
    val playersNumber = bind { playersNumberProp }

    val selectedPlayer = SimpleStringProperty()
    val turn = SimpleStringProperty()

    val battleShipsTypes =
        emptySimpleMapProperty<Int, Int>()
            .putInitials()
            .updateFleetReadiness()

    val playersAndShips = emptySimpleMapProperty<String, MutableList<Ship>>().notifyOnChange()
    val playersNames = emptySimpleListProperty<String>()

    val fleetsReadiness = mutableMapOf<String, MutableMap<Int, SimpleIntegerProperty>>()
    val playersReadiness = ObservableValueMap<String, Boolean>()
    val defeatedPlayers = emptySimpleListProperty<String>()

    private fun SimpleMapProperty<Int, Int>.updateFleetReadiness(): SimpleMapProperty<Int, Int> {
        addListener(
            MapChangeListener {
                fleetsReadiness.keys.forEach { player ->
                    fleetsReadiness[player] = fleetReadiness(battleShipsTypes)
                }
            })

        return this
    }

    fun getWinner(): String = playersNames.first { it !in defeatedPlayers }
    fun lastButNotDefeated(player: String): Boolean {
        return player !in defeatedPlayers &&
                defeatedPlayers.containsAll(playersNames.exclude(player, currentPlayer))
    }

    private fun SimpleMapProperty<Int, Int>.putInitials(): SimpleMapProperty<Int, Int> {
        (0 until shipsTypes).forEach { this[it + 1] = shipsTypes - it }
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
                        playersNames.add(player)
                        setNotReady(player)
                        fleetsReadiness[player] = fleetReadiness(battleShipsTypes)
                        log { "added \"$player\"" }
                    }

                    change.wasRemoved() -> {
                        player {
                            removeFrom(playersNames)
                            removeFrom(playersReadiness)
                            removeFrom(fleetsReadiness)
                            removeFrom(defeatedPlayers)
                        }

                        log { " removed \"$player\"" }
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
        return playersAndShips[currentPlayer]!!.hadHit(shot)
    }

    fun hasNo(enemyToHit: String, hitCoord: Coord): Boolean {
        return hitCoord !in statistics
            .asSequence()
            .filter { it.player == currentPlayer }
            .filter { it.target == enemyToHit }
            .map { it.shot }
    }

    fun hasAllShips(player: String): Boolean {
        val decks = playersAndShips[player]!!.flatten().count()
        val shouldBe = battleShipsTypes.entries.sumOf { it.key * it.value }
        log { "hasAllShips [decks == shouldBe; $decks ? $shouldBe"}
        return decks == shouldBe
    }

    fun hasReady(player: String) = playersReadiness[player]!!
    fun setReady(player: String) { playersReadiness[player] = true }
    fun setNotReady(player: String) { playersReadiness[player] = false }
}

val BattleModel.notAllReady get() =
    playersReadiness.any(::isNotReady) || (playersReadiness.size != playersNumber.value)

val BattleModel.allAreReady get() = playersReadiness.all(::isReady) && playersReadiness.size == playersNumber.value
val BattleModel.thoseAreReady get() = playersReadiness.filter(::isReady).keys.toSet()

private fun isReady(it: Map.Entry<String, Boolean>) = it.value
private fun isNotReady(it: Map.Entry<String, Boolean>) = !it.value

