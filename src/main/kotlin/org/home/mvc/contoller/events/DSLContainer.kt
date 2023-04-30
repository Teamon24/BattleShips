package org.home.mvc.contoller.events

import tornadofx.Component
import tornadofx.FXEvent

@JvmInline
value class DSLContainer<E>(val elements: MutableList<E> = mutableListOf()) {
    operator fun E.unaryPlus() {
        elements.add(this)
    }

    fun clear() {
        elements.clear()
    }

    companion object {
        inline fun Component.eventbus(addEvents: DSLContainer<FXEvent>.() -> Unit) {
            val dslContainer = DSLContainer<FXEvent>()
            dslContainer.addEvents()
            dslContainer.elements.onEach { fire(it) }.clear()
        }
    }
}