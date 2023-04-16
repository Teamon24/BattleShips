package org.home.utils

import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.Pane
import org.home.utils.NodeUtils

object NodeUtils {
    fun getAllNodes(root: Parent): ArrayList<Node> {
        var nodes = ArrayList<Node>()
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
}