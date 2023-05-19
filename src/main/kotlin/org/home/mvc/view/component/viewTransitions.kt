@file:JvmName("ViewTransitionsKt")

package org.home.mvc.view.component

import javafx.event.EventTarget
import javafx.util.Duration
import org.home.mvc.ApplicationProperties.Companion.backButtonText
import org.home.app.di.FxScopes
import org.home.app.di.GameScope
import org.home.mvc.view.NewServerView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.component.Transit.BACKWARD
import org.home.mvc.view.component.Transit.FORWARD
import org.home.mvc.view.component.button.battleButton
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

enum class Transit { FORWARD, BACKWARD }

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

inline fun <reified T : View> View.transitTo(type: Transit = FORWARD) {
    transitLogic<T, Metro>(type)
}

inline fun <reified T : View> View.transferTo(type: Transit = FORWARD) {
    transferLogic<T, Metro>(type)
}

inline fun <reified T : View, reified VT: ViewTransition> View.transitLogic(type: Transit) {
    find<T>().also {
        logTransit(it)
        replaceWith(it, viewTransition<VT>(type))
    }
}

inline fun <reified T : View, reified VT: ViewTransition> View.transferLogic(type: Transit) {
    val view = find<T>(FxScopes.getGameScope())
    view.also {
        logTransit(it)
        replaceWith(it, viewTransition<VT>(type))
    }
}

inline fun <reified VT : ViewTransition> viewTransition(type: Transit) = when (type) {
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
