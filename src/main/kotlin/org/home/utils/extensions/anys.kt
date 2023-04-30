package org.home.utils.extensions

import org.home.utils.threadPrintln
import kotlin.reflect.KClass

inline val Any.className: String get() = this.javaClass.simpleName

object AnysExtensions {


    inline val KClass<*>.name: String
        get() {
            var result = this.toString().replace("class", "").trim()
            result = result.substring(result.lastIndexOf('.') + 1)
            result = result.replace('$', '.')

            return result
        }

    inline val Any.name: String
        get() {
            var result = this.toString().replace("class", "").trim()
            result = result.substring(result.lastIndexOf('.') + 1)
            result = result.replace('$', '.')

            return result
        }

    inline fun <T> T.isNotUnit(function: (T) -> Any) {
        when (this) {
            !is Unit -> function(this)
        }
    }

    inline operator fun <T> T.invoke(body: T.() -> Unit) = this.body()

    fun <T> Int.repeat(n: () -> T) = repeat(this) { n() }

    operator fun <T> T.plus(messages: MutableList<in T>) = messages.also { it.add(0, this) }

    fun <T> T.removeFrom(c: MutableCollection<T>) = c.remove(this)
    fun <T> T.excludeFrom(c: MutableCollection<T>) = c.filter { it != this }
    fun <T> T.removeFrom(c: MutableMap<T, *>) = c.remove(this)

    val Thread.isNotInterrupted get() = !isInterrupted
}


