package org.home.utils

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.exclude
import javafx.css.Styleable
import javafx.scene.control.Labeled
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import javafx.scene.paint.Color.WHITE
import org.home.app.ApplicationProperties.Companion.fillingTransitionTime
import org.home.style.Transition
import org.home.utils.ColorUtils.color
import org.home.style.TransitionDSL.filling
import org.home.style.TransitionDSL.transition
import tornadofx.CssRule
import tornadofx.InlineCss
import tornadofx.addClass
import tornadofx.box
import tornadofx.hasClass
import tornadofx.px
import tornadofx.removeClass
import tornadofx.style

object StyleUtils {
    val Region.backgroundColor: Color get() = background?.fills?.get(0)?.fill?.color ?: WHITE
    val Labeled.textColor: Color get() = textFill?.color ?: WHITE

    fun Region.fillBackground(from: Color = backgroundColor, to: Color) =
        filling(from, to) { backgroundColor += it }

    fun Region.filling(from: Color, to: Color, cssProp: InlineCss.(Color) -> Unit) {
        style {
            filling(this@filling) {
                millis = fillingTransitionTime
                transition(from, to, cssProp)
            }
        }
    }

    fun Transition.textFillTransition() = transition(BLACK, WHITE) { textFill = it }

    fun Styleable.rightPadding(dimension: Int) {
        style {
            padding = box(0.px, dimension.px, 0.px, 0.px)
        }
    }

    fun Styleable.leftPadding(dimension: Int) {
        style {
            padding = box(0.px, 0.px, 0.px, dimension.px)
        }
    }

    fun <T: Styleable> T.toggle(rulesPair: Pair<CssRule, CssRule>) = rulesPair { toggle(first, second) }

    fun <T: Styleable> T.toggle(clazz: CssRule, another: CssRule) {
        hasClass(clazz) {
            removeClass(clazz)
            addClass(another)
        }
    }

    private fun <T: Styleable> T.hasClass(cssRule: CssRule, onTrue: () -> Unit) =
        hasClass(cssRule.name).so(onTrue)

    fun <T: Styleable> T.toggle(rule: CssRule, rules: Collection<CssRule>) {
        val otherRules = rules.exclude(rule)
        hasAnyClass(otherRules) {
            removeClass(it)
            addClass(rule)
        }
    }

    private fun <T: Styleable> T.hasAnyClass(rules: Collection<CssRule>, onTrue: (CssRule) -> Unit) =
        rules.firstOrNull { hasClass(it) }?.apply(onTrue)

}