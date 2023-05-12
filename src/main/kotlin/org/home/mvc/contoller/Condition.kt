package org.home.mvc.contoller

import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.log
import java.util.concurrent.CountDownLatch

class Condition<A>(val name: String, private val accepter: A) {
    private val latch = CountDownLatch(1)
    private val state = false.atomic

    private var afterNotify: A.() -> Unit = {}

    fun notifyUI(afterNotify: A.() -> Unit = {}) {
        this.afterNotify = afterNotify
        latch.countDown()
        state(true)
    }

    fun await() {
        log { "awaiting for $this" }
        latch.await()
        accepter.afterNotify()
    }

    override fun toString(): String {
        return "Condition('$name', ${state()})"
    }

    companion object {
        fun <A> condition(name: String, accepter: A) = Condition(name, accepter)
    }
}