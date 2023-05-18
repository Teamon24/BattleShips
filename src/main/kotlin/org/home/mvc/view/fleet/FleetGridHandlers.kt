package org.home.mvc.view.fleet

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
import org.home.mvc.ApplicationProperties.Companion.incorrectCellRemovingTime
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.Ship
import org.home.mvc.model.Ships
import org.home.mvc.model.toShip
import org.home.mvc.model.withinAnyBorder
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addBorderColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addIncorrectColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addIncorrectHover
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.addSelectionColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeAnyColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeBorderColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeIncorrectColor
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeIncorrectHover
import org.home.mvc.view.fleet.style.FleetGridStyleAddClass.removeSelectionColor
import org.home.style.AppStyles.Companion.emptyCell
import org.home.utils.log
import org.home.utils.logCoordinate
import org.home.utils.threadScopeLaunch
import tornadofx.addClass
import tornadofx.removeClass
import java.util.concurrent.atomic.AtomicBoolean

class FleetGridHandlers(
    private val mouseWentOutOfBound: AtomicBoolean,
    private val startWithinBorder: AtomicBoolean,
    private val beingConstructed: Ship,
    private val ships: Ships,
    private val shipsTypesController: ShipsTypesController
) {

    private val backingHandlers = mutableMapOf<EventType<out MouseEvent>, EventHandler<in MouseEvent>>()

    fun getHandlers(): Map<EventType<out MouseEvent>, EventHandler<in MouseEvent>> {
        return backingHandlers
    }

    fun addDragEnteredHandler(currentCell: FleetCell, gridPane: FleetGrid) {
        val eventType = MouseDragEvent.MOUSE_DRAG_ENTERED
        currentCell.leftClickHandler(eventType) {
            log { "$eventType" }
            if (startWithinBorder() ||
                mouseWentOutOfBound() ||
                beingConstructed.crosses(ships) ||
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

                val lastCell = gridPane.cell(last)
                if (ships.any { ship -> last in ship }) {
                    lastCell.removeIncorrectColor()
                } else {
                    lastCell.removeAnyColor()
                }
            }

            beingConstructed.addIfAbsent(currentCell.coord)

            beingConstructed.hasElement {
                shipsTypesController.validates(beingConstructed).otherwise {
                    val ship = beingConstructed.toShip()
                    gridPane.cell(ship).removeClass(emptyCell)
                    gridPane.cell(ship).addIncorrectHover()
                }
            }

            if (shipsTypesController.validates(beingConstructed)) {
                gridPane.addSelectionColor(beingConstructed)
            } else {
                for (i in 0..beingConstructed.size) {
                    val dropped = beingConstructed.takeLast(i).toShip()
                    val droppedShip = beingConstructed.dropLast(i).toShip()
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
                    gridPane.cell(it).removeSelectionColor()
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
                    val ship = beingConstructed.first().toShip()
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
                shipsTypesController.add(ship.copy())
                currentCell.addSelectionColor()
            }

            return@leftClickHandler
        }
    }

    fun addRightMouseClickHandler(currentCell: FleetCell, gridPane: FleetGrid) {
        currentCell.rightClickHandler(MouseDragEvent.MOUSE_CLICKED) {
            val beingDeletedShip = ships.firstOrNull { currentCell.coord in it }
            if (beingDeletedShip != null) {
                if (beingDeletedShip.hasDecks(1)) {
                    currentCell.removeSelectionColor()
                } else {
                    gridPane.removeSelectionColor(beingDeletedShip)
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

                    gridPane.removeSelectionColor(filter)
                    gridPane.addIncorrectColor(filter)
                    gridPane.addBorderColor(border)
                    delay(incorrectCellRemovingTime)
                    gridPane.removeAnyColor(filter)
                    gridPane.removeBorderColor(border)
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
        backingHandlers[eventType] = handler
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
        this.addEventHandler(eventType, handler)
        backingHandlers[eventType] = handler
    }

    private fun MouseEvent.isPrimary()   = this.button == MouseButton.PRIMARY
    private fun MouseEvent.isSecondary() = this.button == MouseButton.SECONDARY
}




