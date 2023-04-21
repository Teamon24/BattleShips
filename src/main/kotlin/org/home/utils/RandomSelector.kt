package org.home.utils

import com.google.common.collect.Sets
import java.util.*

class RandomSelector(private vararg val lists: List<*>) {
    constructor(lists: List<List<*>>): this(*lists.toTypedArray())

    private val listsIndices = lists.map { it.indices.toSet() }
    private val products = Sets.cartesianProduct(listsIndices).toMutableList()

    fun next(): List<*> {
        val indices = getRandomIndices()
        return get(indices)
    }

    fun hasNext() = products.size > 0

    companion object {
        const val NO_MORE_COMBINATIONS_MESSAGE = "There is no more combinations of random indices"
    }

    fun getRandomIndices(): List<Int> {
        if (products.isEmpty()) throw IllegalStateException(NO_MORE_COMBINATIONS_MESSAGE)
        val n = Random(Random(1).nextLong()).nextInt(products.size)

        val indices = products[n]
        products.removeAt(n)
        return indices
    }

    fun get(indices: Collection<Int>): List<*> {
        val pairs = lists.zip(indices)
        return pairs.map { (list, index) ->
            list[index]
        }
    }
}