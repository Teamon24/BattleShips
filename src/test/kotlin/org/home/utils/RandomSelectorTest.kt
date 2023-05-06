package org.home.utils

import org.home.RandomSelector
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class RandomSelectorTest {

    /**
     * Test for [RandomSelector.get].
     */
    @ParameterizedTest
    @MethodSource("getTestData")
    fun getTest(lists: List<List<*>>, indices: List<Int>, expected: Collection<Int>) {
        val actual = RandomSelector(lists).get(indices)
        Assertions.assertEquals(expected.size, actual.size)
        Assertions.assertTrue(actual.containsAll(expected))
    }

    /**
     * Test for [RandomSelector.next].
     */
    @ParameterizedTest
    @MethodSource("nextTestData")
    fun nextTestWhenNoMoreCombinationsLeft(lists: List<List<*>>) {
        val randomSelector = RandomSelector(lists)
        Assertions.assertThrows(IllegalStateException::class.java) {
            for (i in 1 .. lists.map { it.size }.mult() + 1) {
                randomSelector.getRandomIndices()
            }
        }
    }

    /**
     * Test for [RandomSelector.next].
     */
    @ParameterizedTest
    @MethodSource("nextTestData")
    fun nextTestWhenCombinationsAreLeft(lists: List<List<*>>) {
        val randomSelector = RandomSelector(lists)
        Assertions.assertDoesNotThrow {
            for (i in 1 .. lists.map { it.size }.mult()) {
                randomSelector.getRandomIndices()
            }
        }
    }


    companion object {
        private val lists = listOf(
            listOf(1, 2, 3, 4),
            listOf(5, 6, 7, 8),
            listOf(9, 10, 11, 12),
        )

        private val lists2 = listOf(
            listOf(1, 2),
            listOf(3, 4, 5, 6),
            listOf(7),
        )

        @JvmStatic
        fun getTestData(): Stream<Arguments> {
            val args = mutableListOf<Arguments>()
            for (i in 0 until lists[0].size) {
                args.add(Arguments.of(lists, listOf(i, i, i), lists.map { it[i] }))
            }

            args.add(Arguments.of(lists2, listOf(0, 1, 0), listOf(1, 4, 7)))
            args.add(Arguments.of(lists2, listOf(1, 1, 0), listOf(2, 4, 7)))
            args.add(Arguments.of(lists2, listOf(0, 3, 0), listOf(1, 6, 7)))

            return args.stream()
        }


        @JvmStatic
        fun nextTestData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(lists),
                Arguments.of(lists2),
            )
        }
    }

    private fun Collection<Int>.mult(): Long {
        return fold(1L) { acc, i -> acc * i }
    }
}
