package org.home.style

import javafx.scene.layout.Region
import javafx.scene.paint.Color
import home.Quadruple
import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.then
import home.extensions.BooleansExtensions.yes
import org.home.utils.log
import tornadofx.InlineCss
import tornadofx.onHover
import tornadofx.runLater
import tornadofx.style
import kotlin.concurrent.thread
import kotlin.math.abs

class HoverTransition(
    private val region: Region,
    time: Double,
    private val hoversInfo: Map<Pair<Color, Color>, InlineCss.(Color) -> Unit>
) {
    private val steps = 50
    private val stepSleep = (time / steps).toLong()

    @JvmInline
    value class RGB(val triple: Quadruple<Double, Double, Double, Double>) {
        inline val red: Double get() = triple.first
        inline val green: Double get() = triple.second
        inline val blue: Double get() = triple.third
        inline val opacity: Double get() = triple.forth
    }

    private fun getComponentStep(from: Color, to: Color) =
        RGB(
            Quadruple(
                abs(from.red - to.red) / steps,
                abs(from.green - to.green) / steps,
                abs(from.blue - to.blue) / steps,
                abs(from.opacity - to.opacity) / steps)
        )

    private inline fun colorStepOp(
        from: Color,
        to: Color,
        inc: Int,
        colorStep: Double,
        crossinline colorComponent: Color.() -> Double
    ) =
        if (from.colorComponent() < to.colorComponent()) {
            when (inc) {
                1 -> { color: Color -> 1.0.coerceAtMost(color.colorComponent() + colorStep) }
                -1 -> { color: Color -> 0.0.coerceAtLeast(color.colorComponent() - colorStep) }
                else -> throw RuntimeException("")
            }
        } else {
            when (inc) {
                1 -> { color: Color -> 0.0.coerceAtLeast(color.colorComponent() - colorStep) }
                -1 -> { color: Color -> 1.0.coerceAtMost(color.colorComponent() + colorStep) }
                else -> throw RuntimeException("")
            }
        }


    private fun Color.incr(from: Color, to: Color): Color {
        return Color.color(
            colorStepOp(from, to, 1, getComponentStep(from, to).red) { red }.invoke(this),
            colorStepOp(from, to, 1, getComponentStep(from, to).green) { green }.invoke(this),
            colorStepOp(from, to, 1, getComponentStep(from, to).blue) { blue }.invoke(this),
            colorStepOp(from, to, 1, getComponentStep(from, to).opacity) { opacity }.invoke(this),
        )
    }

    private fun Color.decr(from: Color, to: Color): Color {
        return Color.color(
            colorStepOp(from, to, -1, getComponentStep(from, to).red) { red }.invoke(this),
            colorStepOp(from, to, -1, getComponentStep(from, to).green) { green }.invoke(this),
            colorStepOp(from, to, -1, getComponentStep(from, to).blue) { blue }.invoke(this),
            colorStepOp(from, to, -1, getComponentStep(from, to).opacity) { opacity }.invoke(this),
        )
    }

    private var disabled = false.atomic
    private var isPlaying = false.atomic
    private var hovered = true
    private lateinit var counter: (Int) -> Int
    private var colorCounters = mutableListOf<(Color) -> Color>()
    private val colorIncrementors = hoversInfo.keys.map { getColorInc(it.first, it.second) }.toMutableList()
    private val colorDecrementors = hoversInfo.keys.map { getColorDecr(it.first, it.second) }.toMutableList()
    private val transformets = hoversInfo.values

    private fun getColorInc(from: Color, to: Color) = { c: Color -> c.incr(from, to) }
    private fun getColorDecr(from: Color, to: Color) = { c: Color -> c.decr(from, to) }

    fun disable() {
        log { "${region.name} disabling hover transition" }
        disabled(true)
        thread?.interrupt()
    }

    init {
        region.onHover {
            log { "${region.name} hover transition disabled: $disabled" }
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
            log { "${region.name} playing transition" }
            play()
            log { "${region.name} transition has been played" }
        }
    }

    private fun play() {
        isPlaying(true)
        var count = hovered then 0 or steps

        var colors = when(hovered) {
            true -> hoversInfo.keys.map { it.first }.toMutableList()
            else -> hoversInfo.keys.map { it.second }.toMutableList()
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
                    transformets.zip(colors).forEach { (transformation, color) -> transformation(color) }
                }
            }

            count = counter(count)
        } while (count in 1..steps && !disabled())

        isPlaying(false)
    }
}