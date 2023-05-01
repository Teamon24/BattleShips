package org.home.mvc.view.fleet

import javafx.scene.input.MouseDragEvent
import javafx.scene.layout.GridPane
import kotlinx.coroutines.delay
import org.home.mvc.ApplicationProperties.Companion.delayTime
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.Ship
import org.home.mvc.model.Ships
import org.home.mvc.model.addIfAbsent
import org.home.mvc.model.logShips
import org.home.mvc.model.toShip
import org.home.mvc.model.withinAnyBorder
import org.home.mvc.view.components.GridPaneExtensions.getCell
import org.home.mvc.view.fleet.FleetGridStyleComponent.addBorderColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.addIncorrectColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.addSelectionColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeBorderColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeIncorrectColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeSelectionColor
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.log
import org.home.utils.singleThreadScope
import java.util.concurrent.atomic.AtomicBoolean

class FleetGridHandlers(
    private val mouseWentOutOfBound: AtomicBoolean,
    private val startWithinBorder: AtomicBoolean,
    private val beingConstructed: Ship,
    private val ships: Ships,
    private val shipsTypesController: ShipsTypesController
) {

    fun addDragEnteredHandler(currentCell: FleetCell, gridPane: GridPane) {
        currentCell.leftClickHandler(MouseDragEvent.MOUSE_DRAG_ENTERED) {
            if (startWithinBorder() ||
                mouseWentOutOfBound() ||
                beingConstructed.crosses(ships) ||
                withinBorder(currentCell, gridPane)
            ) {
                return@leftClickHandler
            }

            if (beingConstructed.isNotEmpty() &&
                beingConstructed.first().withinAnyBorder(ships)
            ) {
                startWithinBorder(true)
                gridPane.removeAnyColor(beingConstructed)
                beingConstructed.clear()
                return@leftClickHandler
            }

            if (beingConstructed.size >= 2 && beingConstructed.secondLast() == currentCell.coord) {
                val last = beingConstructed.last()
                beingConstructed.remove(last)

                val lastCell = gridPane.getCell(last)
                if (ships.any { ship -> last in ship }) {
                    lastCell.removeIncorrectColor()
                } else {
                    lastCell.removeAnyColor()
                }

                for (i in 0..beingConstructed.size) {
                    val dropped = beingConstructed.takeLast(i).toShip()
                    val droppedShip = beingConstructed.dropLast(i).toShip()
                    if (shipsTypesController.validates(droppedShip)) {
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

                gridPane.addIncorrectColor(beingConstructed)

                log("being constructed ship:")
                return@leftClickHandler
            }

            beingConstructed.addIfAbsent(currentCell.coord)

            if (shipsTypesController.validates(beingConstructed)) {
                gridPane.addSelectionColor(beingConstructed)
            } else {
                gridPane.addIncorrectColor(beingConstructed)
            }

            log("being constructed ship:")
        }
    }

    private fun log(title: String) {
        log { title }
        log { beingConstructed }
    }

    fun addDragReleasedHandler(currentCell: FleetCell, gridPane: GridPane) {
        currentCell.leftClickHandler(MouseDragEvent.MOUSE_DRAG_RELEASED) {
            if (startWithinBorder()) {
                startWithinBorder(false)
                return@leftClickHandler
            }

            if (mouseWentOutOfBound()) {
                return@leftClickHandler
            }

            if (beingConstructed.crosses(ships)) {
                val flatten = ships.flatten()
                val toRemove = beingConstructed.filter { it !in flatten }

                toRemove.forEach {
                    gridPane.getCell(it).removeSelectionColor()
                    gridPane.getCell(it).addIncorrectColor()
                }

                singleThreadScope {
                    delay(delayTime)
                    toRemove.forEach { gridPane.getCell(it).removeIncorrectColor() }
                    gridPane.removeIncorrectColor(beingConstructed)
                    beingConstructed.clear()
                }

                return@leftClickHandler
            }

            if (shipsTypesController.validates(beingConstructed)) {
                shipsTypesController.count(beingConstructed)
                ships.addIfAbsent(beingConstructed.copy())
                gridPane.removeIncorrectColor(beingConstructed)
            } else {
                gridPane.removeAnyColor(beingConstructed)
            }
            beingConstructed.clear()
            log { "ships: $ships" }
        }
    }

    fun addRightMouseClickHandler(currentCell: FleetCell) {
        currentCell.rightClickHandler(MouseDragEvent.MOUSE_CLICKED) {
            val gridPane = currentCell.parent as GridPane

            val beingDeletedShip = ships.firstOrNull { currentCell.coord in it }

            if (beingDeletedShip != null) {

                if (beingDeletedShip.hasDecks(1)) {
                    currentCell.removeSelectionColor()
                } else {
                    gridPane.removeSelectionColor(beingDeletedShip)
                }

                ships.remove(beingDeletedShip)
                shipsTypesController.discount(beingDeletedShip)
                logShips(ships, "after deleting")
                return@rightClickHandler
            }
        }
    }

    fun addLeftMouseClickHandler(currentCell: FleetCell, gridPane: GridPane) {
        currentCell.leftClickHandler(MouseDragEvent.MOUSE_CLICKED) {

            if (withinBorder(currentCell, gridPane)) return@leftClickHandler

            val ship = currentCell.coord.toShip()

            if (shipsTypesController.validates(ship)) {
                shipsTypesController.count(ship)
                ships.addIfAbsent(ship)
                currentCell.addSelectionColor()
                logShips(ships, "after adding")
            }

            return@leftClickHandler
        }
    }

    private fun withinBorder(currentCell: FleetCell, gridPane: GridPane): Boolean {
        val first = ships.firstOrNull { ship -> currentCell.coord.withinAnyBorder(ship) }
        return when (first) {
            null -> false
            else -> true.also {
                beingConstructed.addIfAbsent(currentCell.coord)
                startWithinBorder(true)
                singleThreadScope {
                    val filter = beingConstructed.filter { it !in ships.flatten() }

                    val border = first.border(
                        gridPane.rowCount - 1,
                        gridPane.columnCount - 1
                    )

                    gridPane.removeSelectionColor(filter)
                    gridPane.addIncorrectColor(filter)
                    gridPane.addBorderColor(border)
                    delay(delayTime)
                    gridPane.removeAnyColor(filter)
                    gridPane.removeBorderColor(border)
                    startWithinBorder(false)
                    beingConstructed.clear()
                }
            }
        }
    }

    private fun Ship.secondLast() = this[size - 2]

}
