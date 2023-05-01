package org.home.style

import javafx.css.Styleable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import org.home.utils.NodeUtils
import tornadofx.CssRule
import tornadofx.addClass
import tornadofx.box
import tornadofx.getChildList
import tornadofx.px
import tornadofx.style

object StyleUtils {

    fun Node.addChildrenClass(vararg cssRule: CssRule) {
        this.getChildList()?.forEach {
            it.addClass(*cssRule)
        }
    }

    fun Node.addChildrenProperty(pos: Pos) {
        getChildList()?.forEach {
            it.apply {
                style {
                    alignment = pos
                }
            }
        }
    }

    fun Parent.addPropertyRecursively(pos: Pos) {
        NodeUtils.getAllNodes(this).forEach {
            it.apply {
                style { alignment = pos }
            }
        }
    }

    fun Parent.addClassRecursively(vararg cssRules: CssRule): Parent {
        cssRules.forEach { this.addClass(it) }
        this.addAllChildrenClass(*cssRules)
        return this
    }

    fun Parent.addAllChildrenClass(vararg cssRules: CssRule): Parent {
        NodeUtils.getAllNodes(this).forEach {
            cssRules.forEach { cssRule ->
                it.addClass(cssRule)
            }
        }
        return this
    }

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
}