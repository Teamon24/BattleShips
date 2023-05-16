@file:JvmName("ViewTransitionsKt")

package org.home.mvc.view.components

import javafx.event.EventTarget
import javafx.util.Duration
import org.home.mvc.ApplicationProperties.Companion.backButtonText
import org.home.app.di.FxScopes
import org.home.mvc.view.components.Transit.BACKWARD
import org.home.mvc.view.components.Transit.FORWARD
import org.home.utils.componentName
import org.home.utils.log
import tornadofx.View
import tornadofx.ViewTransition
import tornadofx.ViewTransition.Cover
import tornadofx.ViewTransition.Direction.RIGHT
import tornadofx.ViewTransition.Direction.LEFT
import tornadofx.ViewTransition.Reveal
import tornadofx.ViewTransition.Metro
import tornadofx.ViewTransition.Slide
import tornadofx.ViewTransition.Swap
import tornadofx.ViewTransition.Fade
import tornadofx.action
import tornadofx.find
import tornadofx.point
import tornadofx.seconds

private const val SLIDE_TIME = 0.3
private const val METRO_DISTANCE = 0.5

enum class Transit { FORWARD, BACKWARD }

fun slide(seconds: Duration) = Slide(seconds)
fun reveal(seconds: Duration) = Reveal(seconds, LEFT)
fun swap(seconds: Duration, point: Double) = Swap(seconds, LEFT, point(point, point))
fun fade(seconds: Duration) = Fade(seconds)
fun cover(seconds: Duration) = Cover(seconds, LEFT)
fun metro(seconds: Duration, distance: Double) = Metro(seconds, LEFT, distance)

fun Slide  .back() = Slide  (duration, RIGHT)
fun Reveal .back() = Reveal (duration, RIGHT)
fun Cover  .back() = Cover  (duration, RIGHT)
fun Swap   .back() = Swap   (duration, RIGHT, scale)
fun Metro  .back() = Metro  (duration, RIGHT, this.distancePercentage)

private val duration = 0.5.seconds

val slide  = slide(duration)
val metro  = metro(duration, METRO_DISTANCE)
val cover  = cover(duration)
val reveal = reveal(duration)
val fade   = fade(duration)
val swap   = swap(duration, 0.9)

inline fun <reified T : View> EventTarget.backTransitButton(
    from: View,
    text: String = backButtonText,
    crossinline body: () -> Unit = {}
) = battleButton(text) {
        action {
            body()
            from.transitTo<T>(BACKWARD)
        }
    }

inline fun <reified T : View> View.transitTo(transit: Transit = FORWARD) {
    transitLogic<T, Metro>(transit)
}

inline fun <reified T : View> View.transferTo(transit: Transit = FORWARD) {
    transferLogic<T, Metro>(transit)
}

inline fun <reified T : View, reified VT: ViewTransition> View.transitLogic(transit: Transit) {
    find<T>().also {
        logTransit(it)
        replaceWith(it, ward<VT>(transit))
    }
}

inline fun <reified T : View, reified VT: ViewTransition> View.transferLogic(transit: Transit) {
    find<T>(FxScopes.getGameScope()).also {
        logTransit(it)
        replaceWith(it, ward<VT>(transit))
    }
}

inline fun <reified VT : ViewTransition> ward(transit: Transit) = when (transit) {
    FORWARD -> forward<VT>(FORWARD)
    BACKWARD -> backward<VT>(BACKWARD)
}

inline fun <reified VT : ViewTransition> forward(transit: Transit) =
    when (VT::class) {
        Cover::class -> cover
        Fade::class -> fade
        Metro::class -> metro
        Reveal::class -> reveal
        Slide::class -> slide
        Swap::class -> swap
        else -> throw RuntimeException(message<VT>(transit))
    }

inline fun <reified VT : ViewTransition> backward(transit: Transit) =
    when (VT::class) {
        Cover::class -> cover.back()
        Fade::class -> fade
        Metro::class -> metro.back()
        Reveal::class -> reveal.back()
        Slide::class -> slide.back()
        Swap::class -> swap.back()
        else -> throw RuntimeException(message<VT>(transit))
    }

inline fun <reified VT : ViewTransition> message(transit: Transit) =
    "there is no when-branch for ${VT::class} in #${transit.name.lowercase()}"


inline fun <reified T : View> View.logTransit(replacement: T) {
    log {
        "|////////////////////////////////////////////////| $componentName |/////| |> ${replacement.componentName}"
    }
}
