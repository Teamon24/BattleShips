package org.home.utils.extensions

import org.home.utils.threadPrintln
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object AtomicBooleansExtensions {
    inline val Boolean.atomic get() = AtomicBoolean(this)

    fun AtomicBoolean.toggleAfter(time: Long): AtomicBoolean {
        thread(name = "TOGGLER") {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < time) {}
            set(!get())
            threadPrintln("Toggled")
        }
        return this
    }

    inline operator fun AtomicBoolean.invoke() = this.get()
    inline operator fun AtomicBoolean.invoke(value: Boolean) = this.set(value)
}
