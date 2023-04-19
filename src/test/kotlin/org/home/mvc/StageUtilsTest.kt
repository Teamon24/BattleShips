package org.home.mvc

import org.home.mvc.StageUtils.ViewInitialPosition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.awt.Dimension
import java.util.stream.Stream


/**
 * Test for [StageUtils].
 */
class StageUtilsTest {

    /**
     * Test for [StageUtils.getInitialPosition]. */
    @ParameterizedTest
    @MethodSource("getInitialPositionTestData")
    fun getInitialPositionTest(screenSize: () -> Dimension, player: Int, players: Int, expected: ViewInitialPosition) {
        val actual = StageUtils.getInitialPosition(player, players, screenSize)
        Assertions.assertEquals(expected.start, actual.start)
    }

    /**
     * Test for [StageUtils.twoFactors]. */
    @ParameterizedTest
    @MethodSource("twoFactorsTestData")
    fun twoFactorsTest(number: Int, expected: Pair<Int, Int>) {
        val actual = StageUtils.twoFactors(number)
        Assertions.assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun twoFactorsTestData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(4, 2 to 2),
                Arguments.of(7, 3 to 3),
                Arguments.of(8, 3 to 3),
                Arguments.of(9, 3 to 3)
            )
        }

        @JvmStatic
        fun getInitialPositionTestData(): Stream<Arguments> {
            val (width, height) = 1920.0 to 1080.0
            val n = 4
            val screenSize = { Dimension(width.toInt(), height.toInt()) }
            return Stream.of(
                Arguments.of(screenSize, 0, n, ViewInitialPosition(width/n, height/n, 0.0, 0.0)),
                Arguments.of(screenSize, 1, n, ViewInitialPosition(width/n, height/n, width/2, 0.0)),
                Arguments.of(screenSize, 2, n, ViewInitialPosition(width/n, height/n, 0.0, height/2 )),
                Arguments.of(screenSize, 3, n, ViewInitialPosition(width/n, height/n, width/2, height/2)),
            )
        }
    }
}

