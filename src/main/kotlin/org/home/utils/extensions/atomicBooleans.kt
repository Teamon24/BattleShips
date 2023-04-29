package org.home.utils.extensions

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object AtomicBooleansExtensions {
    inline val Boolean.atomic get() = AtomicBoolean(this)

    fun AtomicBoolean.toggleAfter(time: Long): AtomicBoolean {
        thread {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < time) {}
            val toggledValue = !this.get()
            this.set(toggledValue)
        }
        return this
    }

    inline operator fun AtomicBoolean.invoke() = this.get()
    inline operator fun AtomicBoolean.invoke(value: Boolean) = this.set(value)
}
