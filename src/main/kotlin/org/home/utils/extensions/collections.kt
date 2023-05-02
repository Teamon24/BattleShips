package org.home.utils.extensions

object CollectionsExtensions {

    @JvmName("excludeByKey")
    fun <K, V> Map<K, V>.exclude(exception: K) = filter { it.key != exception }

    fun <K, V> Map<K, V>.excludeAll(exceptions: Collection<K>) = filter { it.key !in exceptions }

    fun <K> Collection<K>.exclude(exception: K) = filter { it != exception }
    fun <K> Collection<K>.exclude(vararg exceptions: K) = filter { it !in exceptions }

    fun <K, V> Map<K, V>.shuffledKeys() = keys.toMutableList().shuffled().toMutableList()

    fun <E> List<E>.asMutableList(): MutableList<E> {
        if (this is MutableList<E>) {
            return this
        }
        return this.toMutableList()
    }

    val <T> Collection<T>.hasElements get() = size > 1
    val <T> Collection<T>.hasElement get() = size == 1
}