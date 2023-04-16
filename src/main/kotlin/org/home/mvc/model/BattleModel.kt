package org.home.mvc.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import tornadofx.ViewModel
import tornadofx.toObservable


class BattleModel : ViewModel() {

    companion object {
        const val i = 10
        const val shipsTypes = 4
    }

    fun equalizeSizes() {
        fleetGridHeight.value = fleetGridWidth.value
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

    val fleetGridWidth = bind { widthProp }
    val fleetGridHeight = bind { heightProp }
    val playersNumber = bind { playersNumberProp }

    val playersAndShips = bind { shipsProp }.apply(::playersNamesListener)
    val playersNames = bind { playersNamesProp }

    val battleShipsTypes = bind { shipsTypesProp }

    init {
        playersAndShips["Astra-1"] = mutableListOf(Ship(1 to 1), Ship(3 to 3))
        playersAndShips["Astra-2"] = mutableListOf(Ship(1 to 1, 1 to 2, 1 to 3))
        playersAndShips["Astra-3"] = mutableListOf(Ship(3 to 1, 3 to 2, 3 to 3, 3 to 4))
    }

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
}

fun <K, V> SimpleMapProperty<K, V>.copy(): ObservableMap<K, V> {
    return toMutableMap().toObservable()
}

