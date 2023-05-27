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
import org.home.utils.ColorStepper
import org.home.utils.ColorTransition
import org.home.utils.log
import tornadofx.InlineCss
import tornadofx.onHover
import tornadofx.runLater
import tornadofx.style
import kotlin.concurrent.thread

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


typealias ColorCounter = (Color) -> Color
typealias ColorCounters = MutableList<ColorCounter>

abstract class Transition(protected val region: Region) {

    enum class StepOperation { INCR, DECR }

    private val colorStepper = ColorStepper()
    protected val steps = colorStepper.steps

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

    protected var disabled = false.atomic
    protected var isPlaying = false.atomic
    protected lateinit var counter: (Int) -> Int
    private var onFinish: () -> Unit = {}

    private var thread: Thread? = null

    abstract fun enable()
    abstract fun getCounter(): Int
    abstract fun getColors(): MutableList<Color>

    fun add(colorTransition: ColorTransition, cssProp: InlineCss.(Color) -> Unit) {
        colorTransitionInfo.add(colorTransition)
        cssProps.add(cssProp)
        colorStepper.addStep(colorTransition)
        colorIncrementors.add(colorStepper.getColorInc(colorTransition))
        colorDecrementors.add(colorStepper.getColorDecr(colorTransition))
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