package org.home.mvc.view.fleet

import javafx.scene.input.MouseDragEvent
import javafx.scene.layout.GridPane
import kotlinx.coroutines.delay
import org.home.ApplicationProperties.Companion.delayTime
import org.home.mvc.contoller.ShipsTypesController
import org.home.mvc.model.Ship
import org.home.mvc.model.Ships
import org.home.mvc.model.addIfAbsent
import org.home.mvc.model.logShips
import org.home.mvc.model.toShip
import org.home.mvc.model.withinAnyBorder
import org.home.mvc.view.components.getCell
import org.home.mvc.view.fleet.FleetGridStyleComponent.addBorderColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.addIncorrectColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.addSelectionColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeAnyColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeBorderColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeIncorrectColor
import org.home.mvc.view.fleet.FleetGridStyleComponent.removeSelectionColor
import org.home.utils.functions.invoke
import org.home.utils.log
import org.home.utils.functions.singleThreadScope
import java.util.concurrent.atomic.AtomicBoolean

class FleetGridHandlers(
    private val mouseWentOutOfBound: AtomicBoolean,
    private val startWithinBorder: AtomicBoolean,
    private val beingConstructedShip: Ship,
    private val ships: Ships,
    private val shipsTypesController: ShipsTypesController,
) {

    fun addDragEnteredHandler(currentCell: FleetCell, gridPane: GridPane) {
        currentCell.leftClickHandler(MouseDragEvent.MOUSE_DRAG_ENTERED) {
            if (startWithinBorder() ||
                mouseWentOutOfBound() ||
                beingConstructedShip.crosses(ships) ||
                withinBorder(currentCell, gridPane)
            ) {
                return@leftClickHandler
            }

            if (beingConstructedShip.isNotEmpty() &&
                beingConstructedShip.first().withinAnyBorder(ships)
            ) {
                startWithinBorder(true)
                gridPane.removeAnyColor(beingConstructedShip)
                beingConstructedShip.clear()
                return@leftClickHandler
            }

            if (beingConstructedShip.size >= 2 && beingConstructedShip.secondLast() == currentCell.coord) {
                val last = beingConstructedShip.last()
                beingConstructedShip.remove(last)

                val lastCell = gridPane.getCell(last)
                if (ships.any { ship -> last in ship }) {
                    lastCell.removeIncorrectColor()
                } else {
                    lastCell.removeAnyColor()
                }

                for (i in 0..beingConstructedShip.size) {
                    val dropped = beingConstructedShip.takeLast(i).toShip()
                    val droppedShip = beingConstructedShip.dropLast(i).toShip()
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
                gridPane.addIncorrectColor(beingConstructedShip)

                log("being constructed ship:")
                return@leftClickHandler

            }

            beingConstructedShip.addIfAbsent(currentCell.coord)

            if (shipsTypesController.validates(beingConstructedShip)) {
                gridPane.addSelectionColor(beingConstructedShip)
            } else {
                gridPane.addIncorrectColor(beingConstructedShip)
            }

            log("being constructed ship:")
        }
    }

    private fun log(title: String) {
        log { title }
        log { beingConstructedShip }
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

            if (beingConstructedShip.crosses(ships)) {
                val flatten = ships.flatten()
                val toRemove = beingConstructedShip.filter { it !in flatten }

                toRemove.forEach {
                    gridPane.getCell(it).removeSelectionColor()
                    gridPane.getCell(it).addIncorrectColor()
                }

                singleThreadScope {
                    delay(delayTime)
                    toRemove.forEach { gridPane.getCell(it).removeIncorrectColor() }
                    gridPane.removeIncorrectColor(beingConstructedShip)
                    beingConstructedShip.clear()
                }

                return@leftClickHandler
            }

            if (shipsTypesController.validates(beingConstructedShip)) {
                shipsTypesController.count(beingConstructedShip)
                ships.addIfAbsent(beingConstructedShip.copy())
                gridPane.removeIncorrectColor(beingConstructedShip)
                beingConstructedShip.clear()
            } else {
                gridPane.removeAnyColor(beingConstructedShip)
                beingConstructedShip.clear()
            }
            logShips(ships, "after adding")
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
                beingConstructedShip.addIfAbsent(currentCell.coord)
                startWithinBorder(true)
                singleThreadScope {
                    val filter = beingConstructedShip.filter { it !in ships.flatten() }

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
                    beingConstructedShip.clear()
                }
            }
        }
    }

    private fun Ship.secondLast() = this[size - 2]

}
