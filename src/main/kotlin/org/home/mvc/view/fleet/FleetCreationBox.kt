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
import org.home.app.ApplicationProperties.Companion.delayTime
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.BattleModel
import org.home.mvc.model.Ship
import org.home.mvc.model.copy
import org.home.mvc.view.components.getCell
import org.home.mvc.view.fleet.FleetBoxStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.FleetBoxStyleComponent.removeIncorrectColor
import org.home.style.AppStyles
import org.home.utils.atomic
import org.home.utils.invoke
import org.home.utils.singleThread
import tornadofx.add

class FleetCreationBox(currentPlayer: String, model: BattleModel) : VBox() {

    private var shipsTypesController = ShipsTypesController(model.battleShipsTypes.copy())
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

    private val fleetBoxHandlers = FleetBoxHandlers(
        mouseWentOutOfBound,
        startWithinBorder,
        currentShip,
        ships,
        shipsTypesController
    )

    private val components = FleetBoxCreator(model.fleetGridHeight.value, model.fleetGridWidth.value)

    init {
        alignment = Pos.CENTER

        val grid = components
            .fleetGrid()
            .addFleetCellClass(AppStyles.emptyFleetCell)
            .also { add(it) }

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
        fleetBoxHandlers.addDragEnteredHandler(this, gridPane)
        fleetBoxHandlers.addDragReleasedHandler(this, gridPane)
    }

    private fun FleetCell.oneClickHandler() {

        val gridPane = this@oneClickHandler.parent as GridPane

        fleetBoxHandlers.addLeftMouseClickHandler(this, gridPane)
        fleetBoxHandlers.addRightMouseClickHandler(this)
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
            singleThread {
                delay(delayTime)
                removeAnyColor(currentShip.filter { it !in ships.flatten() })
                ships.forEach { this.removeIncorrectColor(it) }
                currentShip.clear()
            }
            mouseWentOutOfBound(true)
        }
    }
}
