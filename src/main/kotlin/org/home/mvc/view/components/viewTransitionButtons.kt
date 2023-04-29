package org.home.mvc.view.components

import javafx.event.EventTarget
import tornadofx.Scope
import tornadofx.UIComponent
import tornadofx.ViewTransition.Slide
import tornadofx.action
import tornadofx.button
import tornadofx.find
import kotlin.reflect.KClass


private const val SLIDE_TIME = 0.3

val slide = slide(SLIDE_TIME)
val backSlide = slide(SLIDE_TIME).right()

fun <T: UIComponent> EventTarget.transitButton(from: UIComponent, to: T, text: String, body: () -> Unit = {}) =
    button(text) {
        action {
            body()
            from.replaceWith(to, slide)
        }
    }


fun EventTarget.backTransitButton(
    from: UIComponent,
    toClass: () -> KClass<out UIComponent>,
    text: String = "Назад",
    body: () -> Unit = {},
) =
    transitLogic(from, toClass, text, backSlide, body)

fun EventTarget.backTransitButton(
    from: UIComponent,
    toClass: KClass<out UIComponent>,
    text: String = "Назад",
    body: () -> Unit = {},
) =
    transitLogic(from, toClass, text, backSlide, body)

fun EventTarget.transitButton(
    from: UIComponent,
    toClass: KClass<out UIComponent>,
    text: String,
    body: () -> Unit = {},
) =
    transitLogic(from, toClass, text, slide, body)

private fun EventTarget.transitLogic(
    from: UIComponent,
    toClass: KClass<out UIComponent>,
    text: String,
    transition: Slide,
    body: () -> Unit = {},
) =
    button(text) {
        action {
            body()
            from.replaceWith(toClass, transition)
        }
    }

private inline fun EventTarget.transitLogic(
    from: UIComponent,
    crossinline toClass: () -> KClass<out UIComponent>,
    text: String,
    transition: Slide,
    crossinline body: () -> Unit = {},
) =
    button(text) {
        action {
            body()
            from.replaceWith(toClass(), transition)
        }
    }


//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//WITH SCOPE
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

fun <T: UIComponent> EventTarget.transferButton(from: UIComponent, toClass: KClass<T>, text: String, body: () -> Unit = {}) =
    transferLogic(Scope(), from, toClass, text, body)

fun EventTarget.backTransferButton(from: UIComponent, toClass: KClass<out UIComponent>, text: String = "Назад") =
    transferLogic(Scope(), from, toClass, text)

private  fun <T: UIComponent> EventTarget.transferLogic(
    scope: Scope ,
    from: UIComponent,
    toClass: KClass<T>,
    text: String,
    body: () -> Unit = {},
) =
    button(text) {
        action {
            body()
            from.replaceWith(find(toClass, scope), slide)
        }
    }
