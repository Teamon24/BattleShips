package org.home.mvc.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import org.home.net.FleetSettingsMessage
import tornadofx.ViewModel
import tornadofx.toObservable


class BattleModel : ViewModel() {

    companion object {
        const val i = 7
        const val shipsTypes = 4
    }

    fun equalizeSizes() {
        height.value = width.value
    }

    private fun <K, V> emptySimpleMapProperty() =
        SimpleMapProperty<K, V>(FXCollections.observableHashMap())

    private val widthProp = SimpleIntegerProperty(i)
    private val heightProp = SimpleIntegerProperty(i)
    private val playersNumberProp = SimpleIntegerProperty(6)
    private val playersNamesProp = SimpleListProperty<String>()

    private val playersAndShipsProp =
        emptySimpleMapProperty<String, MutableList<Ship>>()

    private val shipsTypesProp = emptySimpleMapProperty<Int, Int>().apply(::putInitials)

    val width = bind { widthProp }
    val height = bind { heightProp }

    val playersNumber = bind { playersNumberProp }
    val playersAndShips = bind { playersAndShipsProp }.apply(::notifyOnChange)
    val playersNames = bind { playersNamesProp }

    val battleShipsTypes = bind { shipsTypesProp }

    private fun putInitials(map: SimpleMapProperty<Int, Int>) {
        (0 until shipsTypes).forEach { map[it + 1] = shipsTypes - it }
    }

    val readyPlayers = mutableMapOf<String, SimpleBooleanProperty>()



    private fun notifyOnChange(
        map: SimpleMapProperty<String, MutableList<Ship>>
    ) {
        map.addListener(MapChangeListener { change ->
            val newPlayer = change.key
            when {
                change.wasAdded() -> {
                    playersNames.add(newPlayer)
                    readyPlayers[newPlayer] = SimpleBooleanProperty(false)
                }
                change.wasRemoved() -> {
                    playersNames.remove(newPlayer)
                    readyPlayers.remove(newPlayer)
                }
            }
        })
    }

    fun put(receive: FleetSettingsMessage) {
        width.value = receive.width
        height.value = receive.height
        battleShipsTypes.putAll(receive.shipsTypes)
    }
}

val MutableMap<String, SimpleBooleanProperty>.thoseAreReady get() = run { filter { it.value.value }.keys.toSet() }

fun <K, V> SimpleMapProperty<K, V>.copy(): ObservableMap<K, V> {
    return toMutableMap().toObservable()
}

