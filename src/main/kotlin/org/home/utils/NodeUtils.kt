package org.home.utils

import home.extensions.BooleansExtensions.so
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import org.home.mvc.view.fleet.FleetGrid
import org.home.utils.NodeUtils

object NodeUtils {
    fun getAllNodes(root: Parent): ArrayList<Node> {
        val nodes = ArrayList<Node>()
        fun recurseNodes(node: Node) {
            nodes.add(node)
            if(node is Parent)
                for(child in node.childrenUnmodifiable) {
                    recurseNodes(child)
                }
        }
        recurseNodes(root)
        return nodes
    }

    fun <T : Node> T.disable()                  = apply { isDisable = true }
    fun BorderPane.disable()                    = apply { center.isDisable = true }
    fun FleetGrid.disableIf(condition: Boolean) = apply { condition.so { isDisable = true } }
    fun <T : Node> T.enable()                   = apply { isDisable = false }
    fun BorderPane.enable()                     = apply { center.isDisable = false }
    fun <T : Node> Map<String, T>.disable()     = values.forEach { it.disable() }
    fun <T : Node> Map<String, T>.enable()      = values.forEach { it.enable() }
}