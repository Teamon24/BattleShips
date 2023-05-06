package org.home.utils.extensions

import kotlin.reflect.KClass


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

    inline val Thread.isNotAlive get() = !isAlive

    inline val Any.refClass get() = name.split("@")[0]
    inline val Any.refNumber get() = name.split("@")[1]

    inline fun <T> T.isNotUnit(function: (T) -> Unit) {
        when (this) {
            !is Unit -> function(this)
        }
    }

    inline fun <T> Int.repeat(function: () -> T) = repeat(this) { function() }

    inline operator fun <T> T?.invoke(body: T.() -> Unit) = if (this != null) this.body() else Unit
    inline operator fun <F, S> Pair<F, S>.invoke(body: (Pair<F, S>) -> Unit) = body(this)

    inline operator fun <T> T.plus(messages: MutableList<in T>) = messages.also { it.add(0, this) }

    inline fun <T> T.removeFrom(collection: MutableCollection<T>) = this.also { collection.remove(it) }
    inline fun <T> T.excludeFrom(collection: MutableCollection<T>) = collection.filter { it != this }
    inline fun <T> T.removeFrom(map: MutableMap<T, *>) = map.remove(this)

    inline fun <T> T.notIn(collection: Collection<T>) = this !in collection
}


inline val Any.className: String get() = this.javaClass.simpleName

