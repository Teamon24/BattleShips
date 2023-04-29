package org.home.mvc.view.components

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.layout.GridPane
import org.home.style.AppStyles
import org.home.mvc.model.Coord
import tornadofx.addClass
import tornadofx.opcr

fun marginGrid(op: GridPane.() -> Unit = {}) = GridPane().apply {
    addClass(AppStyles.gridMargin)
    op()
}

fun EventTarget.centerGrid(op: GridPane.() -> Unit = {}) =
    marginGrid(op).apply { addClass(AppStyles.centerGrid) }

fun EventTarget.marginGrid(op: GridPane.() -> Unit = {}) = opcr(
        this,
        GridPane().apply { addClass(AppStyles.gridMargin) },
        op)

fun GridPane.getCell(cell: Coord): Node {
    return getCell(cell.first, cell.second)
}

fun GridPane.getCell(row: Int, col: Int): Node {
    for (node in children) {
        if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
            return node
        }
    }
    throw RuntimeException("There is no cell with coordinates ($row, $col)")
}

fun GridPane.removeColumn(column: Int) {
    this.children.removeIf { node ->
        GridPane.getColumnIndex(node) == column
    }
}


data class Row(val index: Int, val gridPane: GridPane)

fun cell(row: Int, column: Int, node: () -> Node) {
    val child = node()
    setIndices(child, row, column)
}

fun Row.col(column: Int, node: GridPane.() -> Node) {
    val row = this
    val child = row.gridPane.node()
    setIndices(child, row.index, column)
}

fun GridPane.row(index: Int, function: Row.() -> Unit) {
    Row(index, this).function()
}

fun getIndices(it: Node): Coord {
    return GridPane.getRowIndex(it) to GridPane.getColumnIndex(it)
}

fun setIndices(child: Node, row: Int, column: Int) {
    GridPane.setRowIndex(child, row)
    GridPane.setColumnIndex(child, column)
}

fun setIndices(child: Node, coord: Coord) {
    GridPane.setRowIndex(child, coord.first)
    GridPane.setColumnIndex(child, coord.second)
}

fun <T: GridPane> T.transpose(): T {
    children.forEach {
        val (row, col) = getIndices(it)
        setIndices(it, col, row)
    }

    return this
}