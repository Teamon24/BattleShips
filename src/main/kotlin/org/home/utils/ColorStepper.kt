package org.home.utils

import javafx.scene.paint.Color
import org.home.app.ApplicationProperties.Companion.transitionSteps
import org.home.style.Transition.StepOperation
import org.home.style.Transition.StepOperation.DECR
import org.home.style.Transition.StepOperation.INCR
import org.home.utils.ColorStepper.ColorComponent.BLUE
import org.home.utils.ColorStepper.ColorComponent.GREEN
import org.home.utils.ColorStepper.ColorComponent.OPACITY
import org.home.utils.ColorStepper.ColorComponent.RED
import kotlin.math.abs

typealias ColorTransition = Pair<Color, Color>
typealias ColorComponentKey = Pair<ColorStepper.ColorComponent, ColorTransition>
typealias ColorStep = Double
typealias ColorComponentValue = Double

class ColorStepper(val steps: Int = transitionSteps) {
    enum class ColorComponent(val getter: Color.() -> ColorComponentValue) {
        RED({red}),
        GREEN({green}),
        BLUE({blue}),
        OPACITY({opacity});

        operator fun invoke(color: Color) = getter(color)
    }

    private lateinit var colorTransition: ColorTransition
    private val colorSteps: HashMap<ColorComponentKey, ColorStep> = hashMapOf()

    fun getColorInc(colorTransition: ColorTransition) = { c: Color -> c.incr(colorTransition) }
    fun getColorDecr(colorTransition: ColorTransition) = { c: Color -> c.decr(colorTransition) }

    private fun Color.incr(colorTransition: ColorTransition): Color {
        return Color.color(
            increment(RED,     colorTransition),
            increment(GREEN,   colorTransition),
            increment(BLUE,    colorTransition),
            increment(OPACITY, colorTransition),
        )
    }

    private fun Color.decr(colorTransition: ColorTransition): Color {
        return Color.color(
            decrement(RED,     colorTransition),
            decrement(GREEN,   colorTransition),
            decrement(BLUE,    colorTransition),
            decrement(OPACITY, colorTransition),
        )
    }

    private fun Color.decrement(comp: ColorComponent, colorTransition: ColorTransition) =
        makeAStep(comp, DECR, colorTransition)

    private fun Color.increment(comp: ColorComponent, colorTransition: ColorTransition) =
        makeAStep(comp, INCR, colorTransition)

    private fun Color.makeAStep(component: ColorComponent,
                                counter: StepOperation,
                                colorTransition: ColorTransition
    ): ColorComponentValue {

        val colorStep = colorSteps[component to colorTransition]!!
        val from = colorTransition.first
        val to = colorTransition.second
        return when (counter) {
            INCR -> when(component(from) < component(to)) {
                true -> 1.0.coerceAtMost(component(this) + colorStep)
                else -> 0.0.coerceAtLeast(component(this) - colorStep)
            }
            DECR -> when(component(from) < component(to)) {
                true -> 0.0.coerceAtLeast(component(this) - colorStep)
                else -> 1.0.coerceAtMost(component(this) + colorStep)
            }
        }
    }

    fun set(colorTransition: ColorTransition): ColorStepper {
        this.colorTransition = colorTransition

        this[RED     to colorTransition] = step(RED)
        this[GREEN   to colorTransition] = step(GREEN)
        this[BLUE    to colorTransition] = step(BLUE)
        this[OPACITY to colorTransition] = step(OPACITY)
        return this
    }

    private operator fun set(comp: Pair<ColorComponent, ColorTransition>, value: Double) {
        colorSteps[comp] = value
    }

    private fun step(component: ColorComponent) =
        abs(component(colorTransition.first) - component(colorTransition.second)) / steps
}