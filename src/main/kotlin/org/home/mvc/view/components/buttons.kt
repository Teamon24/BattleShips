package org.home.mvc.view.components

import javafx.event.EventTarget
import org.home.view.right
import org.home.view.slide
import tornadofx.UIComponent
import tornadofx.ViewTransition.Slide
import tornadofx.action
import tornadofx.button
import kotlin.reflect.KClass

private val slide = slide(0.2)
private val backSlide = slide(0.2).right()

fun <T : UIComponent> EventTarget.backTransit(from: UIComponent, toClass: KClass<T>, text: String = "Назад") =
    transitLogic(from, toClass, text, backSlide)

fun <T: UIComponent> EventTarget.transit(from: UIComponent, to: T, text: String, body: () -> Unit = {}) = transitLogic(from, to, text, slide, body)

fun <T : UIComponent> EventTarget.transit(from: UIComponent, toClass: KClass<T>, text: String, body: () -> Unit = {}) =
    transitLogic(from, toClass, text, slide, body)

private fun <T: UIComponent> EventTarget.transitLogic(
    from: UIComponent,
    to: T,
    text: String,
    slide: Slide,
    body: () -> Unit = {}
) =
    button(text) {
        action {
            body()
            from.replaceWith(to, slide)
        }
    }

private fun <T : UIComponent> EventTarget.transitLogic(
    from: UIComponent,
    toClass: KClass<T>,
    text: String,
    transition: Slide,
    body: () -> Unit = {}
) =
    button(text) {
        action {
            body()
            from.replaceWith(toClass, transition)
        }
    }
