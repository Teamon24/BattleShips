package org.home.mvc.contoller

import org.home.utils.log
import java.util.concurrent.CountDownLatch

class Condition<A>(val name: String, private val accepter: A) {
    private val state = CountDownLatch(1)

    private var afterNotify: A.() -> Unit = {}

    fun notifyUI(afterNotify: A.() -> Unit = {}) {
        this.afterNotify = afterNotify
        state.countDown()
        log { "$this: true" }
    }

    fun await() {
        log { "awaiting for $this" }
        state.await()
        accepter.afterNotify()
    }

    override fun toString(): String {
        return "Condition('$name', $state)"
    }

    companion object {
        fun <A> condition(name: String, accepter: A) = Condition(name, accepter)
    }
}