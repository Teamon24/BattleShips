package org.home.mvc.view

import home.extensions.AnysExtensions.name
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.stage.Stage
import org.home.app.di.GameScope
import org.home.mvc.ResizeHelper
import org.home.mvc.model.BattleModel
import org.home.utils.log
import tornadofx.View
import tornadofx.box
import tornadofx.px
import tornadofx.style
import kotlin.system.exitProcess

abstract class AbstractGameView(title: String = ""): View(title = title) {

    internal val model: BattleModel by GameScope.inject()
    internal val applicationProperties = model.applicationProperties
    internal val currentPlayer = applicationProperties.currentPlayer

    open fun exit() {
        exitProcess(0)
    }

    override fun onCreate() {
        ResizeHelper.addResizeListener(this.primaryStage)
        draggable(this.root, this.primaryStage, null)
        root.style {
            borderWidth += box(1.px)
        }
    }

    private fun draggable(node: Node, stage: Stage?, alert: Alert?) {
        val xOffset = doubleArrayOf(0.0)
        val yOffset = doubleArrayOf(0.0)
        node.setOnMousePressed { event ->
            log { "${this.name} pressed" }
            if (stage != null && alert == null) {
                xOffset[0] = stage.x - event.screenX
                yOffset[0] = stage.y - event.screenY
            } else if (stage == null && alert != null) {
                xOffset[0] = alert.x - event.screenX
                yOffset[0] = alert.y - event.screenY
            }
        }
        node.setOnMouseDragged { event ->
            log { "${this.name} dragged" }
            if (stage != null && alert == null) {
                stage.x = event.screenX + xOffset[0]
                stage.y = event.screenY + yOffset[0]
            } else if (stage == null && alert != null) {
                alert.x = event.screenX + xOffset[0]
                alert.y = event.screenY + yOffset[0]
            }
        }
    }

}

