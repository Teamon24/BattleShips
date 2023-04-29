package org.home.mvc.view.fleet

import javafx.scene.Node
import javafx.scene.layout.GridPane
import org.home.mvc.model.Coord
import org.home.mvc.model.Ship
import org.home.style.AppStyles
import org.home.mvc.view.components.getCell
import tornadofx.addClass
import tornadofx.removeClass

object FleetGridStyleComponent {

    fun Node.removeAnyColor() = this
        .removeClass(
            AppStyles.chosenFleetCell,
            AppStyles.incorrectFleetCell,
            AppStyles.shipBorderCell,
            AppStyles.titleCell)

    fun Node.addSelectionColor() =  addClass(AppStyles.chosenFleetCell)
    fun Node.addIncorrectColor() =  addClass(AppStyles.incorrectFleetCell)
    fun Node.addBorderColor() = addClass(AppStyles.shipBorderCell)

    fun Node.removeSelectionColor() = removeClass(AppStyles.chosenFleetCell)
    fun Node.removeIncorrectColor() = removeClass(AppStyles.incorrectFleetCell)
    fun Node.removeBorderColor() = removeClass(AppStyles.shipBorderCell)

    fun GridPane.removeIncorrectColor(beingConstructedShip: Ship) {
        beingConstructedShip.forEach {
            getCell(it).removeClass(AppStyles.incorrectFleetCell)
        }
    }

    fun GridPane.addSelectionColor(ship: Ship) {
        ship.forEach {
            getCell(it)
                .removeIncorrectColor()
                .addSelectionColor()
        }
    }

    fun GridPane.addIncorrectColor(ship: Ship) {
        ship.forEach { getCell(it).addIncorrectColor() }
    }

    fun GridPane.removeSelectionColor(collection: Collection<Coord>) {
        collection.forEach { getCell(it).removeSelectionColor() }
    }

    fun GridPane.addIncorrectColor(collection: Collection<Coord>) {
        collection.forEach { getCell(it).addIncorrectColor() }
    }

    fun GridPane.removeAnyColor(collection: Collection<Coord>) {
        collection.forEach { getCell(it).removeAnyColor() }
    }

    fun GridPane.removeBorderColor(collection: Collection<Coord>) {
        collection.forEach { getCell(it).removeBorderColor() }
    }

    fun GridPane.addBorderColor(collection: Collection<Coord>) {
        collection.forEach { getCell(it).addBorderColor() }
    }
}