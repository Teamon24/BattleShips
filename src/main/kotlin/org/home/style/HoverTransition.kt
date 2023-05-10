package org.home.style

import home.extensions.AnysExtensions.name
import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.then
import home.extensions.BooleansExtensions.yes
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import org.home.style.HoverTransition.ColorComponent.RED
import org.home.style.HoverTransition.ColorComponent.GREEN
import org.home.style.HoverTransition.ColorComponent.BLUE
import org.home.style.HoverTransition.ColorComponent.OPACITY
import org.home.style.HoverTransition.Step.DECR
import org.home.style.HoverTransition.Step.INCR
import org.home.utils.log
import tornadofx.InlineCss
import tornadofx.onHover
import tornadofx.runLater
import tornadofx.style
import kotlin.concurrent.thread
import kotlin.math.abs

class HoverTransition(private val region: Region) {

    @JvmInline
    value class FromTo(val fromTo: Pair<Color, Color>)

    enum class ColorComponent(val getter: Color.() -> Double) {
        RED({red}),
        GREEN({green}),
        BLUE({blue}),
        OPACITY({opacity})
    }

    enum class Step { INCR, DECR }

    private val hoversInfo: MutableList<Pair<Color, Color>> = mutableListOf()
    private val steps = 50
    var millis = 0L
    set(value) {
        field = value
        stepSleep = (value / steps)
    }

    private var stepSleep = (millis / steps)

    private val colorIncrementors = mutableListOf<(Color) -> Color>()
    private val colorDecrementors = mutableListOf<(Color) -> Color>()
    private val transformations = mutableListOf<InlineCss.(Color) -> Unit>()
    private val colorSteps = hashMapOf<Pair<ColorComponent, FromTo>, Double>()

    private fun getColorInc(from: Color, to: Color) = { c: Color -> c.incr(from, to) }
    private fun getColorDecr(from: Color, to: Color) = { c: Color -> c.decr(from, to) }

    fun add(fromTo: Pair<Color, Color>, cssProp: InlineCss.(Color) -> Unit) {
        hoversInfo.add(fromTo)

        val from = fromTo.first
        val to = fromTo.second

        colorIncrementors.add(getColorInc(from, to))
        colorDecrementors.add(getColorDecr(from, to))
        transformations.add(cssProp)

        colorSteps[RED      to FromTo(fromTo)] = abs(from.red     - to.red) / steps
        colorSteps[GREEN    to FromTo(fromTo)] = abs(from.green   - to.green) / steps
        colorSteps[BLUE     to FromTo(fromTo)] = abs(from.blue    - to.blue) / steps
        colorSteps[OPACITY  to FromTo(fromTo)] = abs(from.opacity - to.opacity) / steps
    }

    private fun Color.incr(from: Color, to: Color): Color {
        return Color.color(
            increment(RED,     from, to),
            increment(GREEN,   from, to),
            increment(BLUE,    from, to),
            increment(OPACITY, from, to),
        )
    }

    private fun Color.decr(from: Color, to: Color): Color {
        return Color.color(
            decrement(RED,     from, to),
            decrement(GREEN,   from, to),
            decrement(BLUE,    from, to),
            decrement(OPACITY, from, to),
        )
    }

    private fun Color.decrement(component: ColorComponent, from: Color, to: Color) =
        makeAStep(component, DECR, from, to)

    private fun Color.increment(component: ColorComponent, from: Color, to: Color) =
        makeAStep(component, INCR, from, to)

    private fun Color.makeAStep(component: ColorComponent, counter: Step, from: Color, to: Color): Double {

        val colorStep = colorSteps[component to FromTo(from to to)]!!
        val getComponent = component.getter

        return when (counter) {
            INCR -> when(from.getComponent() < to.getComponent()) {
                true -> 1.0.coerceAtMost(getComponent() + colorStep)
                else -> 0.0.coerceAtLeast(getComponent() - colorStep)
            }
            DECR -> when(from.getComponent() < to.getComponent()) {
                true -> 0.0.coerceAtLeast(getComponent() - colorStep)
                else -> 1.0.coerceAtMost(getComponent() + colorStep)
            }
        }
    }

    private var disabled = false.atomic
    private var isPlaying = false.atomic
    private var hovered = true
    private lateinit var counter: (Int) -> Int
    private var colorCounters = mutableListOf<(Color) -> Color>()

    fun disable() {
        log { "${region.name} disabling hover transition" }
        disabled(true)
        thread?.interrupt()
    }

    init {
        region.onHover {
            disabled().otherwise {
                hovered = it
                it yes {
                    colorCounters = colorIncrementors
                    counter = Int::inc
                    if (!isPlaying() && !disabled()) playInThread()
                } no {
                    colorCounters =  colorDecrementors
                    counter = Int::dec
                    if (!isPlaying() && !disabled()) playInThread()
                }
            }
        }
    }


    private var thread: Thread? = null

    private fun playInThread() {
        thread = thread {
            play()
        }
    }

    private fun play() {
        isPlaying(true)
        var count = hovered then 0 or steps

        val colors = when(hovered) {
            true -> hoversInfo.map { it.first }.toMutableList()
            else -> hoversInfo.map { it.second }.toMutableList()
        }

        do {
            try {
                Thread.sleep(stepSleep)
            } catch (e: InterruptedException) {
                log { "${region.name} transition is stopped" }
                disabled(true)
                return
            }
            colorCounters.forEachIndexed { index, colorCounter ->
                colors[index] = colorCounter(colors[index])
            }

            runLater {
                region.style {
                    transformations.zip(colors).forEach { (transformation, color) ->
                        transformation(color)
                    }
                }
            }

            count = counter(count)
        } while (count in 1..steps && !disabled())

        isPlaying(false)
    }
}