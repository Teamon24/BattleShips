package org.home.utils.extensions

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.collections.FXCollections
import tornadofx.toObservable
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

object ObservablePropertiesExtensions {

    fun <K, V> emptySimpleMapProperty() =
        SimpleMapProperty<K, V>(FXCollections.observableHashMap())

    fun <E> emptySimpleListProperty() =
        SimpleListProperty<E>(FXCollections.observableList(mutableListOf()))

    fun <K, V> SimpleMapProperty<K, V>.copy() = toMutableMap().toObservable()


    open class ObservableValueMap<K, V> : HashMap<K, V>() {
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
}