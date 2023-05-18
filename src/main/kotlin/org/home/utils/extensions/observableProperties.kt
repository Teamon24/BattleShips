package org.home.utils.extensions

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import tornadofx.toObservable
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

object ObservablePropertiesExtensions {

    fun <K, V> emptySimpleMapProperty() =
        SimpleMapProperty<K, V>(FXCollections.observableHashMap())

    fun <E> emptySimpleListProperty() =
        SimpleListProperty<E>(FXCollections.observableList(mutableListOf()))

    fun <K, V> SimpleMapProperty<K, V>.copy() = SimpleMapProperty(toMutableMap().toObservable())
}

class ObservableValueMap<K, V> : HashMap<K, V>() {
    private val ps = PropertyChangeSupport(this)

    fun addValueListener(pcl: PropertyChangeListener) = ps.addPropertyChangeListener(pcl)

    override fun put(key: K, new: V): V? {
        if (get(key) != new) {
            val old = super.put(key, new)
            ps.firePropertyChange("map", old, new)
        }
        return new
    }
}

fun <K, V> SimpleMapProperty<K, V>.addMapChange(change: (MapChangeListener.Change<out K, out V>) -> Unit) {
    addListener(MapChangeListener { change(it) })
}