package org.home.mvc.model

import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.isEmpty

typealias Ships = MutableList<Ship>

inline fun Ships.areDestroyed(onTrue: () -> Unit) = isEmpty.so(onTrue)
fun Ships.countType(type: Int) = count { it.size == type }
fun Ships.copy() = map { ship -> ship.copy() }.toMutableList()
fun Ships.fleetReadiness() = associate { it.size to countType(it.size) }.toSortedMap()