package org.home.utils.extensions

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleSetProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import tornadofx.toObservable
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

object ObservablePropertiesExtensions {

    fun <K, V> mapProperty() =
        SimpleMapProperty<K, V>(FXCollections.observableHashMap())

    fun <E> listProperty() =
        SimpleListProperty<E>(FXCollections.observableList(mutableListOf()))

    fun <E> setProperty() =
        SimpleSetProperty<E>(FXCollections.observableSet(hashSetOf()))

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

fun <K, V> ObservableMap<K, V>.onMapChange(change: (MapChangeListener.Change<out K, out V>) -> Unit) {
    addListener(MapChangeListener { change(it) })
}

fun <E> ObservableList<E>.onLisChange(change: (ListChangeListener.Change<out E>) -> Unit) {
    addListener(ListChangeListener { change(it) })
}