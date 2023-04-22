package org.home.net

import org.home.utils.extensions.BooleansExtensions.invoke
import java.util.concurrent.atomic.AtomicBoolean

class Condition(val name: String) {
    private val state = AtomicBoolean(false)
    fun isNotDone() = !state()

    private var afterNotify: () -> Any = {}

    fun notifyUI(afterNotify: () -> Any) {
        this.afterNotify = afterNotify
        state(true)
    }

    companion object {
        fun <T> waitFor(condition: Condition, body: () -> T): T {
            while (condition.isNotDone()) {
                Thread.sleep(100L)
            }
            condition.afterNotify()
            return body()
        }

        fun condition(name: String) = Condition(name)
    }
}