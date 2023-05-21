package org.home.mvc

import home.extensions.AnysExtensions.name
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.stage.Stage
import org.home.app.ApplicationProperties
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.model.BattleViewModel
import org.home.mvc.view.component.ViewSwitch
import org.home.mvc.view.component.button.ViewSwitchButtonController
import org.home.utils.log
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.box
import tornadofx.px
import tornadofx.style
import java.awt.Dimension
import kotlin.math.roundToInt
import kotlin.system.exitProcess

open class GameBean: Component(), ScopedInstance

abstract class GameViewModel: ViewModel() {
    protected val applicationProperties by noScope<ApplicationProperties>()
}

abstract class GameComponent: GameBean() {
    protected val modelView by gameScope<BattleViewModel>()
    val applicationProperties by noScope<ApplicationProperties>()
    protected open val currentPlayer = modelView.getCurrentPlayer()
}

open class GameController : GameComponent()

open class ViewSwitchController : GameComponent() {
    val viewSwitch by noScope<ViewSwitch>()
}

abstract class GameView(title: String = ""): View(title = title) {
    internal val modelView by gameScope<BattleViewModel>()
    internal val applicationProperties by noScope<ApplicationProperties>()
    internal open val currentPlayer = modelView.getCurrentPlayer()

    internal val viewSwitchButtonController by noScope<ViewSwitchButtonController>()
    internal val viewSwitch = viewSwitchButtonController.viewSwitch

    internal inline fun <reified T: GameView> T.currentView(): T = this

    abstract fun onClose()

    override fun onBeforeShow() {
        currentWindow?.setOnCloseRequest { onClose(); exitProcess(0) }
        super.onBeforeShow()
    }

    init {
        primaryStage.setOnCloseRequest { onClose(); exitProcess(0) }
        applicationProperties.players?.also {
            val screenSize = StageUtils.screenSize()
            val shrink = 0.965
            StageUtils.setInitialPosition(
                currentView(),
                applicationProperties.player!!,
                applicationProperties.players!!,
                { screenSize.run { Dimension((width * shrink).roundToInt(), height) } },
                { this.x = this.x + screenSize.width * (1 - shrink) }
            )
        }
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

