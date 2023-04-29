package org.home.mvc.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import org.home.mvc.ApplicationProperties
import org.home.net.action.FleetSettingsAction
import org.home.net.action.HasAShot
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.removeFrom
import org.home.utils.log
import tornadofx.ViewModel
import tornadofx.toObservable
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport


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

    private fun <K, V> emptySimpleMapProperty() =
        SimpleMapProperty<K, V>(FXCollections.observableHashMap())

    private fun <E> emptySimpleListProperty() =
        SimpleListProperty<E>(FXCollections.observableList(mutableListOf()))

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

    val battleIsEnded: Boolean get() = playersNames.size - defeatedPlayers.size == 1
    val onePlayerLeft: Boolean = playersNames.size == 1
    fun getWinner(): String = playersNames.first { it !in defeatedPlayers }

    private fun SimpleMapProperty<Int, Int>.putInitials(): SimpleMapProperty<Int, Int> {
        (0 until shipsTypes).forEach { this[it + 1] = shipsTypes - it }
        return this
    }

    val allAreReady get() = playersReadiness.all(::isReady) && playersReadiness.size == playersNumber.value
    val notAllReady get() = playersReadiness.any(::isNotReady) || (playersReadiness.size != playersNumber.value)

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
                        playersReadiness[player] = false
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
}

fun <K, V> SimpleMapProperty<K, V>.copy() = toMutableMap().toObservable()

private fun isReady(it: Map.Entry<String, Boolean>) = it.value
private fun isNotReady(it: Map.Entry<String, Boolean>) = !it.value

val Map<String, Boolean>.thoseAreReady get() = filter(::isReady).keys.toSet()


class ObservableValueMap<K, V: Comparable<V>> : HashMap<K, V>() {
    private val ps = PropertyChangeSupport(this)

    fun addValueListener(pcl: PropertyChangeListener) = ps.addPropertyChangeListener(pcl)

    override fun put(key: K, value: V): V? {
        if (get(key) != value) {
            val ret = super.put(key, value)
            ps.firePropertyChange("map", ret, value)
        }
        return value
    }
}
