@file:JvmName("ViewTransitionsKt")

package org.home.mvc.view.components

import javafx.event.EventTarget
import org.home.mvc.ApplicationProperties.Companion.backButtonText
import org.home.mvc.view.Scopes
import org.home.utils.componentName
import org.home.utils.log
import tornadofx.View
import tornadofx.ViewTransition
import tornadofx.ViewTransition.Slide
import tornadofx.action
import tornadofx.button
import tornadofx.find
import tornadofx.seconds


private const val SLIDE_TIME = 0.3

fun slide(seconds: Double) = Slide(seconds.seconds)
fun Slide.right() = Slide(this.duration, ViewTransition.Direction.RIGHT)

val forwardSlide = slide(SLIDE_TIME)
val backSlide = slide(SLIDE_TIME).right()

inline fun <reified T: View> EventTarget.backTransitButton(
    from: View,
    text: String = backButtonText,
    crossinline body: () -> Unit = {},
) =
    button(text) {
        action {
            body()
            from.replaceWith(find(T::class), backSlide)
        }
    }

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//WITH SCOPE
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

inline fun <reified T : View> View.transferTo(slide: Slide = forwardSlide) {
    val replacement = find(T::class, Scopes.gameScope)
    log { "|////////////////////////////////////////////////| $componentName |/////| |> ${replacement.componentName}" }
    replaceWith(replacement, slide)
}

inline fun <reified T : View> View.transitTo(slide: Slide = forwardSlide) {
    val replacement = find(T::class)
    log { "|////////////////////////////////////////////////| $componentName |/////| |> ${replacement.componentName}" }
    replaceWith(replacement, slide)
}
