package org.home.mvc

import javafx.scene.Parent
import org.home.utils.log
import tornadofx.View
import java.awt.Dimension
import java.awt.Toolkit
import kotlin.math.roundToInt
import kotlin.math.sqrt

object StageUtils {

    private val noTwoFactorDecomposition = 0 to 0
    val view = object : View() { override val root: Parent get() = TODO() }

    @JvmStatic
    fun main(args: Array<String>) {
        log { "screenSize: ${screenSize()}" }
        println(getInitialPosition(0, 4, ::screenSize))
        println(getInitialPosition(1, 4, ::screenSize))
        println(getInitialPosition(2, 4, ::screenSize))
        println(getInitialPosition(3, 4, ::screenSize))
    }

    fun setInitialPosition(view: View, player: Int, players: Int, screenSize: () -> Dimension, shift: ViewInitialPosition.() -> Unit) {
        val initialPosition = getInitialPosition(player, players, screenSize)
        initialPosition.shift()
        val (width, height, x, y) = initialPosition
        view.primaryStage.height = height
        view.primaryStage.width = width
        view.primaryStage.x = x
        view.primaryStage.y = y
    }


    fun getInitialPosition(player: Int, players: Int, screen: () -> Dimension): ViewInitialPosition {
        val factors = twoFactors(players)
        if (noTwoFactorDecomposition == factors) throw RuntimeException("$players cant be decompose into two factors")
        val (rows, cols) = factors
        val screenSize = screen()
        val height = (screenSize.height / rows).toDouble()
        val width = (screenSize.width / cols).toDouble()
        val x = (player - cols * (player / cols)) * width
        val y = (player / cols) * height
        val viewInitialPosition = ViewInitialPosition(width, height, x, y)
        log { "factors: $factors" }
        log { viewInitialPosition }
        return viewInitialPosition
    }

    fun screenSize() = Toolkit.getDefaultToolkit().screenSize!!

    class ViewInitialPosition(
        val width: Double,
        val height: Double,
        var x: Double,
        var y: Double,
    )
    {
        val start = x to y
        operator fun component1() = width
        operator fun component2() = height
        operator fun component3() = x
        operator fun component4() = y
        override fun toString(): String {
            return "(w=$width, h=$height, x=$x, y=$y)"
        }
    }

    fun twoFactors(players: Int): Pair<Int, Int> {
        val n = getClosestNumberOfSqr(players)
        val sqrt = sqrt(players.toFloat())
        val middle = sqrt.roundToInt()
        if (middle - sqrt == 0f) return middle to middle

        val factors = getFactors(n)
        val decomposition = factors
            .drop(2)
            .apply {
                if (size == 1) {
                    return first() to first()
                }
            }
            .apply { ifEmpty { return noTwoFactorDecomposition } }
            .takeLast(2)

        return decomposition[0] to decomposition[1]
    }

    private fun getClosestNumberOfSqr(number: Int): Int {
        val sqrt = sqrt(number.toFloat())
        var middle = sqrt.roundToInt()
        while (middle * middle < number) {
           middle += 1
        }
        return middle * middle
    }

    private fun getFactors(n: Int): ArrayList<Int> {
        var i = 1
        val arrayListOf = arrayListOf<Int>()
        while (i <= sqrt(n.toDouble())) {
            if (n % i == 0) {
                val div = n / i
                if (i == div) {
                    arrayListOf.add(i)
                } else {
                    arrayListOf.add(i)
                    arrayListOf.add(div)
                }
            }
            ++i
        }

        return arrayListOf
    }
}

operator fun Dimension.component1() = this.width.toDouble()
operator fun Dimension.component2() = this.height.toDouble()