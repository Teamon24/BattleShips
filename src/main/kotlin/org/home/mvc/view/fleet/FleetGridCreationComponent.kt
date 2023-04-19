package org.home.mvc.view.fleet

import javafx.geometry.Pos
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseDragEvent.DRAG_DETECTED
import javafx.scene.input.MouseDragEvent.MOUSE_DRAG_ENTERED
import javafx.scene.input.MouseDragEvent.MOUSE_DRAG_EXITED
import javafx.scene.input.MouseDragEvent.MOUSE_EXITED
import javafx.scene.input.MouseDragEvent.MOUSE_RELEASED
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.delay
import org.home.ApplicationProperties
import org.home.ApplicationProperties.Companion.delayTime

import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.view.components.getCell
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeIncorrectColor
import org.home.style.AppStyles
import org.home.utils.atomic
import org.home.utils.invoke
import org.home.utils.log
import org.home.utils.singleThreadScope
import tornadofx.Controller
import tornadofx.addChildIfPossible

class FleetGridCreationComponent : Controller() {

    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()
    private val currentPlayer: String = applicationProperties.currentPlayer
    private val shipsTypesController : ShipsTypesController by di()

    private val ships = model.playersAndShips.let {
        it[currentPlayer] = mutableListOf()
        it[currentPlayer]!!
    }
    private val currentShip = Ship()

    private var mouseWentOutOfBound = false.atomic()
    private var dragged = false.atomic()
    private var dragExited = false.atomic()
    private var exitHappened = false.atomic()
    private var startWithinBorder = false.atomic()

    private val fleetGridHandlers = FleetGridHandlers(
        mouseWentOutOfBound,
        startWithinBorder,
        currentShip,
        ships,
        shipsTypesController
    )

    private val components = FleetGridCreator(model.height.value, model.width.value)

    val root: VBox = VBox().apply {
        println("${this::class.simpleName}: " +
                "creating fleet grid " +
                "(${model.width.value}, ${model.height.value})")

        alignment = Pos.CENTER

        val grid = components
            .fleetGrid()
            .addFleetCellClass(AppStyles.emptyFleetCell)
            .also { this.addChildIfPossible(it) }

        grid.forEachFleetCells {
            it.setOnDragDetected { startFullDrag() }
            it.dragHandler()
            it.oneClickHandler()
        }

        components.colRange.map {
            val cell = grid.getCell(0, it)
            cell.addEventHandler(MOUSE_DRAG_ENTERED, grid.exitHappened())
            cell.addEventHandler(MOUSE_RELEASED, ::releaseAfterExit)
            cell.addEventHandler(MOUSE_DRAG_EXITED, ::dragExit)
        }

        components.rowRange.map {
            val cell = grid.getCell(it, 0)
            cell.addEventHandler(MOUSE_DRAG_ENTERED, grid.exitHappened())
            cell.addEventHandler(MOUSE_RELEASED, ::releaseAfterExit)
            cell.addEventHandler(MOUSE_DRAG_EXITED, ::dragExit)
        }

        addEventHandler(DRAG_DETECTED) { dragged(true) }
        addEventHandler(MOUSE_DRAG_EXITED, ::dragExit)
        addEventHandler(MOUSE_EXITED, grid.exitHappened())
        addEventHandler(MOUSE_RELEASED, ::releaseAfterExit)
    }

    private fun FleetCell.dragHandler() {
        val gridPane = this@dragHandler.parent as GridPane
        fleetGridHandlers.addDragEnteredHandler(this, gridPane)
        fleetGridHandlers.addDragReleasedHandler(this, gridPane)
    }

    private fun FleetCell.oneClickHandler() {

        val gridPane = this@oneClickHandler.parent as GridPane

        fleetGridHandlers.addLeftMouseClickHandler(this, gridPane)
        fleetGridHandlers.addRightMouseClickHandler(this)
    }

    private fun dragExit(event: MouseDragEvent) {
        event.log()
        dragged(false)
        dragExited(true)
    }

    private fun releaseAfterExit(event: MouseEvent) {
        if (exitHappened()) {
            event.log()
            mouseWentOutOfBound(false)
            startWithinBorder(false)
        }

        dragExited(false)
        dragged(false)
        exitHappened(false)
    }

    private fun GridPane.exitHappened() = { event: MouseEvent ->
        exitHappened(true)
        if (dragged()) {
            event.log()
            singleThreadScope {
                delay(delayTime)
                removeAnyColor(currentShip.filter { it !in ships.flatten() })
                ships.forEach { removeIncorrectColor(it) }
                currentShip.clear()
            }
            mouseWentOutOfBound(true)
        }
    }
}
