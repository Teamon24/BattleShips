package org.home.mvc.view.fleet

import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.input.MouseDragEvent.DRAG_DETECTED
import javafx.scene.input.MouseDragEvent.MOUSE_DRAG_ENTERED
import javafx.scene.input.MouseDragEvent.MOUSE_DRAG_EXITED
import javafx.scene.input.MouseDragEvent.MOUSE_EXITED
import javafx.scene.input.MouseDragEvent.MOUSE_RELEASED
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import kotlinx.coroutines.delay
import org.home.mvc.ApplicationProperties.Companion.incorrectCellRemovingTime
import org.home.mvc.contoller.AbstractGameBean
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.Ship
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeIncorrectColor
import org.home.style.AppStyles
import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.invoke
import org.home.app.AbstractApp.Companion.newGame
import org.home.utils.logCoordinate
import org.home.utils.threadScopeLaunch

class FleetGridController : AbstractGameBean() {

    private val shipsTypesController: ShipsTypesController by newGame()
    private val fleetGridCreator: FleetGridCreator by newGame()

    private val ships by lazy { model.shipsOf(currentPlayer) }
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

    private val titleCellEventHandlers = mutableMapOf<EventType<out MouseEvent>, EventHandler<in MouseEvent>>()
    private val fleetGridEventHandlers = mutableMapOf<EventType<out MouseEvent>, EventHandler<in MouseEvent>>()

    fun fleetGrid() = fleetGridCreator.titledFleetGrid().addFleetCellClass(AppStyles.currentPlayerCell)

    fun activeFleetGrid(): FleetGrid {
        val fleetGrid = fleetGridCreator.titledFleetGrid()
        val exitHappenedHandler = fleetGrid.exitHappened()
        val releaseAfterExitHandler = releaseAfterExit()
        val dragExitHandler = dragExit()
        val dragDetectedHandler = EventHandler { _: MouseEvent -> dragged(true) }

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
                    fleetGridHandlers.addRightMouseClickHandler(cell)
                }

                titleCellEventHandlers.forEach { (event, handler) ->
                    onEachTitleCells { cell -> cell.addEventHandler(event, handler) }
                }

                fleetGridEventHandlers.forEach { (event, handler) ->
                    addEventHandler(event, handler)
                }
            }
    }

    private fun dragExit() = EventHandler { event: MouseEvent ->
        event.logCoordinate()
        dragged(false)
        dragExited(true)
    }

    private fun releaseAfterExit() = EventHandler { event: MouseEvent ->
        if (exitHappened()) {
            event.logCoordinate()
            mouseWentOutOfBound(false)
            startWithinBorder(false)
        }

        dragged(false)
        dragExited(false)
        exitHappened(false)
    }

    private fun GridPane.exitHappened() = EventHandler { event: MouseEvent ->
        exitHappened(true)
        if (dragged()) {
            event.logCoordinate()
            threadScopeLaunch {
                delay(incorrectCellRemovingTime)
                removeAnyColor(currentShip.filter { it !in ships.flatten() })
                ships.forEach { removeIncorrectColor(it) }
                currentShip.clear()
            }
            mouseWentOutOfBound(true)
        }
    }
}
