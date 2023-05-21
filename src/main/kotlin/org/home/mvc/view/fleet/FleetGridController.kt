package org.home.mvc.view.fleet

import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.input.MouseDragEvent.DRAG_DETECTED
import javafx.scene.input.MouseDragEvent.MOUSE_DRAG_ENTERED
import javafx.scene.input.MouseDragEvent.MOUSE_DRAG_EXITED
import javafx.scene.input.MouseDragEvent.MOUSE_EXITED
import javafx.scene.input.MouseDragEvent.MOUSE_RELEASED
import javafx.scene.input.MouseEvent
import org.home.app.di.gameScope
import org.home.mvc.GameController
import org.home.style.AppStyles


class FleetGridController : GameController() {
    private val fleetGridCreator: FleetGridCreator by gameScope()

    private val fleetGridHandlers by gameScope<FleetGridHandlers>()

    private val titleCellEventHandlers = mutableMapOf<EventType<out MouseEvent>, EventHandler<in MouseEvent>>()
    private val fleetGridEventHandlers = mutableMapOf<EventType<out MouseEvent>, EventHandler<in MouseEvent>>()

    fun fleetGrid() = fleetGridCreator.titledFleetGrid().addFleetCellClass(AppStyles.fleetGrid)

    fun activeFleetGrid(): FleetGrid {
        val fleetGrid = fleetGrid()
        val exitHappenedHandler = fleetGridHandlers.exitHappenedHandler(fleetGrid)
        val releaseAfterExitHandler = fleetGridHandlers.releaseAfterExit()
        val dragExitHandler = fleetGridHandlers.dragExit()
        val dragDetectedHandler = fleetGridHandlers.dragDetectedHandler()

        titleCellEventHandlers[MOUSE_DRAG_ENTERED] = exitHappenedHandler
        titleCellEventHandlers[MOUSE_RELEASED] = releaseAfterExitHandler
        titleCellEventHandlers[MOUSE_DRAG_EXITED] = dragExitHandler

        fleetGridEventHandlers[DRAG_DETECTED] = dragDetectedHandler
        fleetGridEventHandlers[MOUSE_DRAG_EXITED] = dragExitHandler
        fleetGridEventHandlers[MOUSE_EXITED] = exitHappenedHandler
        fleetGridEventHandlers[MOUSE_RELEASED] = releaseAfterExitHandler

        return fleetGrid
            .addFleetCellClass(AppStyles.emptyCell)
            .apply {
                forEachFleetCells { cell ->
                    cell.setOnDragDetected { startFullDrag() }
                    fleetGridHandlers.addDragEnteredHandler(cell, this)
                    fleetGridHandlers.addDragReleasedHandler(cell, this)
                    fleetGridHandlers.addLeftMouseClickHandler(cell, this)
                    fleetGridHandlers.addRightMouseClickHandler(cell, this)
                }

                titleCellEventHandlers.forEach { (event, handler) ->
                    onEachTitleCells { cell -> cell.addEventHandler(event, handler) }
                }

                fleetGridEventHandlers.forEach { (event, handler) ->
                    addEventHandler(event, handler)
                }
            }
    }
}
