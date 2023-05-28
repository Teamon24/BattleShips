package org.home.mvc.view.fleet

import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.invoke
import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.CollectionsExtensions.hasElement
import home.extensions.CollectionsExtensions.isNotEmpty
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.delay
import org.home.app.ApplicationProperties.Companion.incorrectCellRemovingTime
import org.home.app.di.gameScope
import org.home.mvc.GameComponent
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.Ship
import org.home.mvc.model.addIfAbsent
import org.home.mvc.model.border
import org.home.mvc.model.copy
import org.home.mvc.model.crosses
import org.home.mvc.model.hasDecks
import org.home.mvc.model.toShip
import org.home.mvc.model.withinAnyBorder
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addIncorrectColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addIncorrectHover
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addSelectionColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeAnyColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeIncorrectColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeIncorrectHover
import org.home.style.AppStyles.Companion.emptyCell
import org.home.utils.log
import org.home.utils.logCoordinate
import org.home.utils.threadScopeLaunch
import tornadofx.addClass
import tornadofx.removeClass

class FleetGridHandlers: GameComponent() {
    private val beingConstructed: Ship = mutableListOf()
    private val shipsTypesController by gameScope<ShipsTypesController>()

    private var mouseWentOutOfBound = false.atomic
    private var dragged = false.atomic
    private var dragExited = false.atomic
    private var exitHappened = false.atomic
    private var startWithinBorder = false.atomic

    private val ships by lazy { modelView.shipsOf(currentPlayer) }

    private val handlers = mutableMapOf<EventType<out MouseEvent>, EventHandler<in MouseEvent>>()

    fun exitHappenedHandler(fleetGrid: FleetGrid) = EventHandler { event: MouseEvent ->
        exitHappened(true)
        dragged().so {
            event.logCoordinate()
            threadScopeLaunch {
                delay(incorrectCellRemovingTime)
                fleetGrid.removeAnyColor(beingConstructed.filter { it !in this.ships.flatten() })
                ships.forEach { fleetGrid.removeIncorrectColor(it) }
                beingConstructed.clear()
            }
            mouseWentOutOfBound(true)
        }
    }

    fun dragDetectedHandler() = EventHandler { _: MouseEvent -> dragged(true) }

    fun dragExit() = EventHandler { event: MouseEvent ->
        event.logCoordinate()
        dragged(false)
        dragExited(true)
    }

    fun releaseAfterExit() = EventHandler { event: MouseEvent ->
        exitHappened().so {
            event.logCoordinate()
            mouseWentOutOfBound(false)
            startWithinBorder(false)
        }

        dragged(false)
        dragExited(false)
        exitHappened(false)
    }


    fun addDragEnteredHandler(currentCell: FleetCell, gridPane: FleetGrid) {
        val eventType = MouseDragEvent.MOUSE_DRAG_ENTERED
        currentCell.leftClickHandler(eventType) {
            log { "$eventType" }
            if (startWithinBorder() ||
                mouseWentOutOfBound() ||
                withinBorder(currentCell, gridPane)
            ) {
                return@leftClickHandler
            }

            if (beingConstructed.isNotEmpty() && beingConstructed.first().withinAnyBorder(ships)) {
                startWithinBorder(true)
                gridPane.removeAnyColor(beingConstructed)
                beingConstructed.clear()
                return@leftClickHandler
            }

            if (beingConstructed.size >= 2 && beingConstructed.secondLast() == currentCell.coord) {
                val last = beingConstructed.last()
                beingConstructed.remove(last)
                gridPane.cell(last).removeAnyColor()
            }

            beingConstructed.addIfAbsent(currentCell.coord)

            beingConstructed.hasElement {
                shipsTypesController.validates(beingConstructed).otherwise {
                    val ship = beingConstructed
                    gridPane.cell(ship).removeClass(emptyCell)
                    gridPane.cell(ship).addIncorrectHover()
                }
            }

            if (shipsTypesController.validates(beingConstructed)) {
                gridPane.addSelectionColor(beingConstructed)
            } else {
                for (i in 0..beingConstructed.size) {
                    val dropped = beingConstructed.takeLast(i)
                    val droppedShip = beingConstructed.dropLast(i)
                    if (shipsTypesController.validates(droppedShip)) {
                        log { "$eventType: beingConstructed - $beingConstructed " }
                        log { "$eventType: dropped - $dropped " }
                        log { "$eventType: validates droppedShip $droppedShip" }
                        gridPane.removeAnyColor(droppedShip)
                        gridPane.addSelectionColor(droppedShip)
                        gridPane.removeAnyColor(dropped)
                        gridPane.addIncorrectColor(dropped)
                        return@leftClickHandler
                    } else {
                        gridPane.removeAnyColor(dropped)
                        gridPane.addIncorrectColor(dropped)
                    }
                }
            }

            log("being constructed ship:")
        }
    }

    private fun log(title: String) {
        log { title }
        log { beingConstructed }
    }

    fun addDragReleasedHandler(currentCell: FleetCell, gridPane: FleetGrid) {
        currentCell.leftClickHandler(MouseDragEvent.MOUSE_DRAG_RELEASED) {
            if (startWithinBorder()) { startWithinBorder(false) }
            if (mouseWentOutOfBound()) { return@leftClickHandler }

            if (beingConstructed.crosses(ships)) {
                val flatten = ships.flatten()
                val toRemove = beingConstructed.filter { it !in flatten }

                toRemove.forEach {
                    gridPane.cell(it).removeAnyColor()
                    gridPane.cell(it).addIncorrectColor()
                }

                toRemove.forEach { gridPane.cell(it).removeIncorrectColor() }
                gridPane.removeIncorrectColor(beingConstructed)
                beingConstructed.clear()

                return@leftClickHandler
            }

            if (shipsTypesController.validates(beingConstructed)) {
                shipsTypesController.add(beingConstructed.copy())
                gridPane.removeIncorrectColor(beingConstructed)
            } else {
                gridPane.removeAnyColor(beingConstructed)
            }

            beingConstructed.isNotEmpty {
                shipsTypesController.validates(beingConstructed).otherwise {
                    val ship = beingConstructed.first()
                    gridPane.cell(ship).removeIncorrectHover()
                    gridPane.cell(ship).addClass(emptyCell)
                }
            }

            beingConstructed.clear()
        }
    }

    fun addLeftMouseClickHandler(currentCell: FleetCell, gridPane: FleetGrid) {
        currentCell.leftClickHandler(MouseDragEvent.MOUSE_CLICKED) {
            if (withinBorder(currentCell, gridPane)) return@leftClickHandler

            val ship = currentCell.coord.toShip()

            if (shipsTypesController.validates(ship)) {
                currentCell.addSelectionColor()
                shipsTypesController.add(ship.copy())
            }

            return@leftClickHandler
        }
    }

    fun addRightMouseClickHandler(currentCell: FleetCell, gridPane: FleetGrid) {
        currentCell.rightClickHandler(MouseDragEvent.MOUSE_CLICKED) {
            val beingDeletedShip = ships.firstOrNull { currentCell.coord in it }
            if (beingDeletedShip != null) {
                if (beingDeletedShip.hasDecks(1)) {
                    currentCell.removeAnyColor()
                } else {
                    gridPane.removeAnyColor(beingDeletedShip)
                }

                shipsTypesController.remove(beingDeletedShip)
                return@rightClickHandler
            }
        }
    }

    private fun withinBorder(currentCell: FleetCell, gridPane: FleetGrid): Boolean {
        val first = ships.firstOrNull { ship -> currentCell.coord.withinAnyBorder(ship) }
        return when (first) {
            null -> false
            else -> true.also {
                beingConstructed.addIfAbsent(currentCell.coord)
                startWithinBorder(true)
                threadScopeLaunch {
                    val filter = beingConstructed.filter { it !in ships.flatten() }

                    val border = first.border(
                        gridPane.rowCount - 1,
                        gridPane.columnCount - 1
                    )

                    gridPane.removeAnyColor(filter)
                    gridPane.addIncorrectColor(filter)
                    gridPane.addIncorrectColor(border)
                    delay(incorrectCellRemovingTime)
                    gridPane.removeAnyColor(filter)
                    gridPane.removeAnyColor(border)
                    startWithinBorder(false)
                    beingConstructed.clear()
                }
            }
        }
    }

    private fun Ship.secondLast() = this[size - 2]

    private inline fun FleetCell.leftClickHandler(
        eventType: EventType<out MouseEvent>,
        crossinline handle: FleetCell.() -> Unit
    ) {
        val handler = EventHandler<MouseEvent> { event ->
            event.isPrimary().so {
                event.logCoordinate()
                this.handle()
            }
        }

        this.addEventHandler(eventType, handler)
        handlers[eventType] = handler
    }

    private inline fun FleetCell.rightClickHandler(
        eventType: EventType<out MouseEvent>,
        crossinline handle: FleetCell.() -> Unit
    ) {
        val handler = EventHandler<MouseEvent> { event ->
            event.isSecondary().so {
                event.logCoordinate()
                this.handle()
            }
        }
        addEventHandler(eventType, handler)
        handlers[eventType] = handler
    }

    private fun MouseEvent.isPrimary()   = this.button == MouseButton.PRIMARY
    private fun MouseEvent.isSecondary() = this.button == MouseButton.SECONDARY
}




