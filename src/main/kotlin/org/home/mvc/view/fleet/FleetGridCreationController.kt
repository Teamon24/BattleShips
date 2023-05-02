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
import org.home.mvc.ApplicationProperties
import org.home.mvc.ApplicationProperties.Companion.delayTime

import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeIncorrectColor
import org.home.style.AppStyles
import org.home.utils.extensions.AtomicBooleansExtensions.atomic
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.logCoordinate
import org.home.utils.singleThreadScope
import tornadofx.Controller
import tornadofx.addChildIfPossible

class FleetGridCreationController : Controller() {

    private val model: BattleModel by di()
    private val applicationProperties: ApplicationProperties by di()
    private val currentPlayer: String = applicationProperties.currentPlayer
    private val shipsTypesController: ShipsTypesController by di()
    private val fleetGridCreator = FleetGridCreator(model)

    private val ships = model.playersAndShips[currentPlayer]!!
    private val currentShip = Ship()

    private var mouseWentOutOfBound = false.atomic
    private var dragged = false.atomic
    private var dragExited = false.atomic
    private var exitHappened = false.atomic
    private var startWithinBorder = false.atomic

    private val fleetGridHandlers = FleetGridHandlers(
        mouseWentOutOfBound,
        startWithinBorder,
        currentShip,
        ships,
        shipsTypesController
    )


    val root: VBox = VBox().apply {
        alignment = Pos.CENTER

        val grid = fleetGridCreator
            .fleetGrid()
            .addFleetCellClass(AppStyles.emptyFleetCell)
            .also { this.addChildIfPossible(it) }

        grid.forEachFleetCells {
            it.setOnDragDetected { startFullDrag() }
            it.dragHandler()
            it.oneClickHandler()
        }

        fleetGridCreator.colRange.map {
            val cell = grid.getCell(0, it)
            cell.addEventHandler(MOUSE_DRAG_ENTERED, grid.exitHappened())
            cell.addEventHandler(MOUSE_RELEASED, ::releaseAfterExit)
            cell.addEventHandler(MOUSE_DRAG_EXITED, ::dragExit)
        }

        fleetGridCreator.rowRange.map {
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
        val gridPane = this.parent as GridPane
        fleetGridHandlers.addDragEnteredHandler(this, gridPane)
        fleetGridHandlers.addDragReleasedHandler(this, gridPane)
    }

    private fun FleetCell.oneClickHandler() {
        val gridPane = this.parent as GridPane
        fleetGridHandlers.addLeftMouseClickHandler(this, gridPane)
        fleetGridHandlers.addRightMouseClickHandler(this)
    }

    private fun dragExit(event: MouseDragEvent) {
        event.logCoordinate()
        dragged(false)
        dragExited(true)
    }

    private fun releaseAfterExit(event: MouseEvent) {
        if (exitHappened()) {
            event.logCoordinate()
            mouseWentOutOfBound(false)
            startWithinBorder(false)
        }

        dragged(false)
        dragExited(false)
        exitHappened(false)
    }

    private fun GridPane.exitHappened() = { event: MouseEvent ->
        exitHappened(true)
        if (dragged()) {
            event.logCoordinate()
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