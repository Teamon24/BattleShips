@file:JvmName("ViewTransitionsKt")

package org.home.mvc.view.component

import tornadofx.ViewTransition.Cover
import tornadofx.ViewTransition.Direction.LEFT
import tornadofx.ViewTransition.Direction.RIGHT
import tornadofx.ViewTransition.Fade
import tornadofx.ViewTransition.Metro
import tornadofx.ViewTransition.Reveal
import tornadofx.ViewTransition.Slide
import tornadofx.ViewTransition.Swap
import javafx.util.Duration
import org.home.mvc.view.component.TransitType.BACKWARD
import org.home.mvc.view.component.TransitType.FORWARD
import tornadofx.ViewTransition
import tornadofx.point
import tornadofx.seconds

enum class TransitType { FORWARD, BACKWARD }

fun slide  (seconds: Duration)                    = Slide  (seconds)
fun reveal (seconds: Duration)                    = Reveal (seconds, LEFT)
fun swap   (seconds: Duration, point: Double)     = Swap   (seconds, LEFT, point( point, point))
fun fade   (seconds: Duration)                    = Fade   (seconds)
fun cover  (seconds: Duration)                    = Cover  (seconds, LEFT)
fun metro  (seconds: Duration, distance: Double)  = Metro  (seconds, LEFT, distance)

fun Slide  .back() = Slide  (duration, RIGHT)
fun Reveal .back() = Reveal (duration, RIGHT)
fun Cover  .back() = Cover  (duration, RIGHT)
fun Swap   .back() = Swap   (duration, RIGHT, scale)
fun Metro  .back() = Metro  (duration, RIGHT, this.distancePercentage)

private val duration = 0.5.seconds
private const val METRO_DISTANCE = 0.5

val slide  = slide(duration)
val metro  = metro(duration, METRO_DISTANCE)
val cover  = cover(duration)
val reveal = reveal(duration)
val fade   = fade(duration)
val swap   = swap(duration, 0.9)

fun metro(type: TransitType = FORWARD) = viewTransition<Metro>(type)

inline fun <reified VT : ViewTransition> viewTransition(type: TransitType) = when (type) {
    FORWARD -> forward<VT>(FORWARD)
    BACKWARD -> backward<VT>(BACKWARD)
}

inline fun <reified VT : ViewTransition> forward(type: TransitType) =
    when (VT::class) {
        Cover::class -> cover
        Fade::class -> fade
        Metro::class -> metro
        Reveal::class -> reveal
        Slide::class -> slide
        Swap::class -> swap
        else -> throw RuntimeException(message<VT>(type))
    }

inline fun <reified VT : ViewTransition> backward(type: TransitType) =
    when (VT::class) {
        Cover::class -> cover.back()
        Fade::class -> fade
        Metro::class -> metro.back()
        Reveal::class -> reveal.back()
        Slide::class -> slide.back()
        Swap::class -> swap.back()
        else -> throw RuntimeException(message<VT>(type))
    }

inline fun <reified VT : ViewTransition> message(type: TransitType) =
    "there is no when-branch for ${VT::class} in #${type.name.lowercase()}"
