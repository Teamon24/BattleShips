package org.home.net

import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.log
import java.util.concurrent.atomic.AtomicBoolean

class Condition<A>(val name: String, private val accepter: A) {


    private val state = AtomicBoolean(false)
    private fun isNotDone() = !state()

    private var afterNotify: A.() -> Unit = {}

    fun notifyUI(afterNotify: A.() -> Unit = {}) {
        this.afterNotify = afterNotify
        state(true)
        log { "$this: true" }
    }

    fun await() {
        log { "awaiting for $this" }
        while (this.isNotDone()) { Thread.sleep(50L) }
        accepter.afterNotify()
    }

    override fun toString(): String {
        return "Condition('$name', $state)"
    }

    companion object {
        fun <A> condition(name: String, accepter: A) = Condition(name, accepter)
    }
}