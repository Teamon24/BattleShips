package org.home.utils

@JvmInline
value class DSLContainer<E>(val elements: MutableList<E> = mutableListOf()) {
    operator fun E.unaryPlus() { elements.add(this) }
    fun clear() { elements.clear() }
}