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
import org.home.mvc.ApplicationProperties.Companion.transitionSteps
import org.home.style.Transition.ColorComponent
import org.home.style.Transition.ColorComponent.RED
import org.home.style.Transition.ColorComponent.GREEN
import org.home.style.Transition.ColorComponent.BLUE
import org.home.style.Transition.ColorComponent.OPACITY
import org.home.style.Transition.StepOperation.DECR
import org.home.style.Transition.StepOperation.INCR
import org.home.utils.log
import tornadofx.InlineCss
import tornadofx.onHover
import tornadofx.runLater
import tornadofx.style
import kotlin.concurrent.thread
import kotlin.math.abs

class FillTransition(region: Region): Transition(region) {
    override fun enable() {
        colorCounters = colorIncrementors
        counter = Int::inc
        playInThread()
    }

    override fun getCounter() = 0
    override fun getColors() = colorTransitionInfo.map { it.first }.toMutableList()
}

class HoverTransition(region: Region) : Transition(region) {

    var hovered: Boolean = false

    override fun enable() {
        super.region.onHover {
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

    override fun getCounter() = hovered then 0 or steps

    override fun getColors() = when(hovered) {
        true -> colorTransitionInfo.map { it.first }.toMutableList()
        else -> colorTransitionInfo.map { it.second }.toMutableList()
    }
}

typealias ColorComponentKey = Pair<ColorComponent, ColorTransition>
typealias ColorStep = Double
typealias ColorComponentValue = Double
typealias ColorTransition = Pair<Color, Color>
typealias ColorCounter = (Color) -> Color
typealias ColorCounters = MutableList<ColorCounter>

abstract class Transition(protected val region: Region) {

    enum class ColorComponent(val getter: Color.() -> ColorComponentValue) {
        RED({red}),
        GREEN({green}),
        BLUE({blue}),
        OPACITY({opacity})
    }

    enum class StepOperation { INCR, DECR }

    protected val steps = transitionSteps

    var millis = 0L
        set(value) {
            field = value
            stepSleep = (value / steps)
        }

    private var stepSleep = (millis / steps)

    protected val colorTransitionInfo: MutableList<ColorTransition> = mutableListOf()
    protected val colorIncrementors: ColorCounters = mutableListOf()
    protected val colorDecrementors: ColorCounters = mutableListOf()
    protected var colorCounters: ColorCounters = mutableListOf()

    private val cssProps: MutableList<InlineCss.(Color) -> Unit> = mutableListOf()
    private val colorSteps: HashMap<ColorComponentKey, ColorStep> = hashMapOf()

    protected var disabled = false.atomic
    protected var isPlaying = false.atomic
    protected lateinit var counter: (Int) -> Int
    private var onFinish: () -> Unit = {}

    private var thread: Thread? = null

    abstract fun enable()
    abstract fun getCounter(): Int
    abstract fun getColors(): MutableList<Color>

    private fun getColorInc(from: Color, to: Color) = { c: Color -> c.incr(from, to) }
    private fun getColorDecr(from: Color, to: Color) = { c: Color -> c.decr(from, to) }

    fun add(colorTransition: ColorTransition, cssProp: InlineCss.(Color) -> Unit) {
        colorTransitionInfo.add(colorTransition)
        cssProps.add(cssProp)

        val from = colorTransition.first
        val to = colorTransition.second

        colorIncrementors.add(getColorInc(from, to))
        colorDecrementors.add(getColorDecr(from, to))

        colorSteps[RED      to colorTransition] = abs(from.red     - to.red) / steps
        colorSteps[GREEN    to colorTransition] = abs(from.green   - to.green) / steps
        colorSteps[BLUE     to colorTransition] = abs(from.blue    - to.blue) / steps
        colorSteps[OPACITY  to colorTransition] = abs(from.opacity - to.opacity) / steps
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

    private fun Color.decrement(comp: ColorComponent, from: Color, to: Color) = makeAStep(comp, DECR, from, to)
    private fun Color.increment(comp: ColorComponent, from: Color, to: Color) = makeAStep(comp, INCR, from, to)

    private fun Color.makeAStep(component: ColorComponent,
                                counter: StepOperation,
                                from: Color,
                                to: Color): ColorComponentValue {

        val colorStep = colorSteps[component to (from to to)]!!
        val getComponent = component.getter

        return when (counter) {
            INCR -> when(from.getComponent() < to.getComponent()) {
                true -> 1.0.coerceAtMost(this.getComponent() + colorStep)
                else -> 0.0.coerceAtLeast(this.getComponent() - colorStep)
            }
            DECR -> when(from.getComponent() < to.getComponent()) {
                true -> 0.0.coerceAtLeast(this.getComponent() - colorStep)
                else -> 1.0.coerceAtMost(this.getComponent() + colorStep)
            }
        }
    }

    fun disable() {
        log { "${region.name} disabling hover transition" }
        disabled(true)
        thread?.interrupt()
    }


    protected fun playInThread() {
        thread = thread(block = ::play)
    }

    private fun play() {
        isPlaying(true)
        var count = getCounter()
        val colors = getColors()

        do {
            try {
                Thread.sleep(stepSleep)
            } catch (e: InterruptedException) {
                log { "${region.name} transition is stopped" }
                disabled(true)
                isPlaying(false)
                return
            }

            colorCounters.forEachIndexed { index, colorCounter ->
                colors[index] = colorCounter(colors[index])
            }

            runLater {
                region.style(append = true) {
                    cssProps.zip(colors).forEach { (transformation, color) ->
                        transformation(color)
                    }
                }
            }

            count = counter(count)
        } while (count in 1..steps && !disabled())

        isPlaying(false)
        onFinish()
    }

    fun onFinish(function: () -> Unit) {
        this.onFinish = function
    }
}