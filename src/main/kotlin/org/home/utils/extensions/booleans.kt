package org.home.utils.extensions

import java.util.concurrent.atomic.AtomicBoolean

object BooleansExtensions {
    fun Boolean.atomic() = AtomicBoolean(this)

    operator fun AtomicBoolean.invoke() = this.get()
    operator fun AtomicBoolean.invoke(value: Boolean) = this.set(value)

    inline infix fun <T> Boolean.then(body: () -> T): T? {
        if (this) {
            return body()
        }
        return null
    }

    inline infix fun <T> T?.or(other: () -> T): T {
        return this ?: other()
    }
}
