package org.home.utils.functions

@JvmName("excludeByKey")
fun <K, V> Map<K, V>.exclude(exception: K): Map<K, V> {
    return filter { it.key != exception }
}

@JvmName("excludeByValue")
fun <K, V> Map<K, V>.exclude(exception: V): Map<K, V> {
    return filter { it.value != exception }
}

fun <K> Collection<K>.exclude(exception: K): Collection<K> {
    return filter { it != exception }
}