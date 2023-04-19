package org.home.mvc.view.components

import javafx.event.EventTarget
import javafx.scene.control.Button
import tornadofx.Scope
import tornadofx.UIComponent
import tornadofx.ViewTransition.Slide
import tornadofx.action
import tornadofx.button
import tornadofx.find
import kotlin.reflect.KClass


val slide = slide(0.2)
val backSlide = slide(0.2).right()


fun <T: UIComponent> EventTarget.transit(from: UIComponent, to: T, text: String, body: () -> Unit = {}) =
    button(text) {
        action {
            body()
            from.replaceWith(to, slide)
        }
    }




fun EventTarget.backTransit(from: UIComponent, toClass: KClass<out UIComponent>, text: String = "Назад", body: () -> Unit = {}) =
    transitLogic(from, toClass, text, backSlide, body)

fun EventTarget.transit(from: UIComponent, toClass: KClass<out UIComponent>, text: String, body: () -> Unit = {}) =
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


//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//WITH SCOPE
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

fun <T: UIComponent> EventTarget.transfer(from: UIComponent, toClass: KClass<T>, text: String, body: () -> Unit = {}) =
    transferLogic(Scope(), from, toClass, text, body)

fun EventTarget.backTransfer(from: UIComponent, toClass: KClass<out UIComponent>, text: String = "Назад") =
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
