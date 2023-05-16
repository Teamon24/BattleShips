package org.home.mvc.view.component

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.layout.GridPane
import org.home.mvc.model.Coord
import org.home.style.AppStyles
import tornadofx.addClass
import tornadofx.opcr

object GridPaneExtensions {

    inline fun EventTarget.centerGrid(op: GridPane.() -> Unit = {}) = marginGrid(op).addClass(AppStyles.centerGrid)

    inline fun EventTarget.marginGrid(op: GridPane.() -> Unit = {}) =
        opcr(
            this,
            GridPane().addClass(AppStyles.gridMargin),
            op
        )

    fun GridPane.getCell(cell: Coord) = getCell(cell.first, cell.second)

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

    inline fun cell(row: Int, column: Int, node: () -> Node) {
        val child = node()
        child.setIndices(row, column)
    }

    inline fun Row.col(column: Int, node: GridPane.() -> Node) {
        val row = this
        val child = row.gridPane.node()
        child.setIndices(row.index, column)
    }

    inline fun GridPane.row(index: Int, function: Row.() -> Unit) {
        Row(index, this).function()
    }

    fun Node.getIndices(): Coord {
        return GridPane.getRowIndex(this) to GridPane.getColumnIndex(this)
    }

    fun Node.setIndices(row: Int, column: Int) {
        GridPane.setRowIndex(this, row)
        GridPane.setColumnIndex(this, column)
    }

    fun Node.setIndices(coord: Coord) {
        GridPane.setRowIndex(this, coord.first)
        GridPane.setColumnIndex(this, coord.second)
    }

    fun <T : GridPane> T.transpose(): T {
        children.forEach {
            val (row, col) = it.getIndices()
            it.setIndices(col, row)
        }

        return this
    }
}