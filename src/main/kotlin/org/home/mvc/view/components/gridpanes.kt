package org.home.mvc.view.components

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.layout.GridPane
import org.home.mvc.model.Coord
import org.home.style.AppStyles
import tornadofx.addClass
import tornadofx.opcr

object GridPaneExtensions {

    inline fun marginGrid(op: GridPane.() -> Unit = {}) =
        GridPane()
            .addClass(AppStyles.gridMargin)
            .op()

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
        setIndices(child, row, column)
    }

    inline fun Row.col(column: Int, node: GridPane.() -> Node) {
        val row = this
        val child = row.gridPane.node()
        setIndices(child, row.index, column)
    }

    inline fun GridPane.row(index: Int, function: Row.() -> Unit) {
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

    fun <T : GridPane> transpose(t: T): T {
        t.children.forEach {
            val (row, col) = getIndices(it)
            setIndices(it, col, row)
        }

        return t
    }

}