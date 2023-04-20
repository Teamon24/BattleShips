package org.home.mvc.view

import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import tornadofx.gridpane
import tornadofx.opcr
import kotlin.math.exp

class ZoomableScrollPane(private val target: Node) : ScrollPane() {
    private var scaleValue = 0.7
    private val zoomIntensity = 0.04
    private val zoomNode: Node

    init {
        zoomNode = Group(target)
        content = outerNode(zoomNode)
        isPannable = true
        isFitToHeight = true //center
        isFitToWidth = true //center
        updateScale()
//        hbarPolicy = ScrollBarPolicy.AS_NEEDED
//        vbarPolicy = ScrollBarPolicy.AS_NEEDED
        hbarPolicy = ScrollBarPolicy.NEVER;
        vbarPolicy = ScrollBarPolicy.NEVER;
    }

    private fun outerNode(node: Node): Node {
        val outerNode = centeredNode(node)
        outerNode.onScroll = EventHandler { e: ScrollEvent ->
            e.consume()
            onScroll(e.textDeltaY, Point2D(e.x, e.y))
        }
        return outerNode
    }

    private fun centeredNode(node: Node): Node {
        val vBox = VBox(node)
        vBox.alignment = Pos.CENTER
        return vBox
    }

    private fun updateScale() {
        target.scaleX = scaleValue
        target.scaleY = scaleValue
    }

    private fun onScroll(wheelDelta: Double, mousePoint: Point2D) {
        val zoomFactor = exp(wheelDelta * zoomIntensity)
        val innerBounds = zoomNode.layoutBounds
        val viewportBounds = viewportBounds

        // calculate pixel offsets from [0, 1] range
        val valX = hvalue * (innerBounds.width - viewportBounds.width)
        val valY = vvalue * (innerBounds.height - viewportBounds.height)
        scaleValue *= zoomFactor
        updateScale()
        layout() // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        val posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint))

        // calculate adjustment of scroll position (pixels)
        val adjustment = target.localToParentTransform.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1))

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        val updatedInnerBounds = zoomNode.boundsInLocal
        hvalue = (valX + adjustment.x) / (updatedInnerBounds.width - viewportBounds.width)
        vvalue = (valY + adjustment.y) / (updatedInnerBounds.height - viewportBounds.height)
    }

    companion object {
        fun EventTarget.zoomableScrollPane(
            target: Node,
            op: ZoomableScrollPane.() -> Unit
        ): ZoomableScrollPane {
            return opcr(this, ZoomableScrollPane(target), op)
        }
    }
}