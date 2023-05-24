package org.home.utils

@JvmInline
value class DSLContainer<E>(val elements: MutableList<E> = mutableListOf()) {
    operator fun E.unaryPlus() { elements.add(this) }
}

inline fun <E> dslContainer(add: DSLContainer<E>.() -> Unit,
                            after: DSLContainer<E>.() -> Unit) = DSLContainer<E>().apply(add).after()

inline fun <E> dslElements(add: DSLContainer<E>.() -> Unit,
                           after: List<E>.() -> Unit = {}) = DSLContainer<E>().run { add(); elements.apply(after) }
