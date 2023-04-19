package org.home.mvc.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import org.home.net.FleetSettingsMessage
import tornadofx.ViewModel
import tornadofx.onChange
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
    private val playersNumberProp = SimpleIntegerProperty(2)
    private val playersNamesProp = SimpleListProperty<String>()

    private val shipsProp =
        emptySimpleMapProperty<String, MutableList<Ship>>()

    private val shipsTypesProp = emptySimpleMapProperty<Int, Int>().apply(::putInitials)

    val width = bind { widthProp }.apply {
        onChange {
            println("new width: $it")
        }
    }
    val height = bind { heightProp }.apply {
        onChange {
            println("new height: $it")
        }
    }
    val playersNumber = bind { playersNumberProp }

    val playersAndShips = bind { shipsProp }.apply(::playersNamesListener)
    val playersNames = bind { playersNamesProp }

    val battleShipsTypes = bind { shipsTypesProp }

    private fun putInitials(map: SimpleMapProperty<Int, Int>) {
        (0 until shipsTypes).forEach { map[it + 1] = shipsTypes - it }
    }

    private fun playersNamesListener(map: SimpleMapProperty<String, MutableList<Ship>>) {
        map.addListener(MapChangeListener { change ->
            when {
                change.wasAdded() -> playersNames.add(change.key)
                change.wasRemoved() -> playersNames.remove(change.key)
            }
        })
    }

    fun put(receive: FleetSettingsMessage) {
        width.value = receive.width
        height.value = receive.height
        battleShipsTypes.putAll(receive.shipsTypes)
    }
}

fun <K, V> SimpleMapProperty<K, V>.copy(): ObservableMap<K, V> {
    return toMutableMap().toObservable()
}

