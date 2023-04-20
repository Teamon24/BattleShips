package org.home.mvc.view.battle

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.paint.Color
import javafx.util.Callback
import org.home.mvc.model.BattleModel
import org.home.style.AppStyles
import tornadofx.style

fun markReadyPlayers(model: BattleModel): (ListView<String>) -> ListCell<String?> =
        {
            object : ListCell<String?>() {
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        text = null
                        style { backgroundColor += Color.WHITE }
                    }
                    if (model.readyPlayers.filter { it.value.value }.keys.contains(item)) {
                        style { backgroundColor += AppStyles.chosenCellColor }
                    } else {
                        style { backgroundColor += Color.WHITE }
                    }
                    text = item
                }
            }
        }

class MarkReadyPlayersCells(val model: BattleModel) : Callback<ListView<String>, ListCell<String>> {
    override fun call(param: ListView<String>): ListCell<String> {
        return object : ListCell<String>() {
            override fun updateItem(playerName: String?, empty: Boolean) {
                super.updateItem(playerName, empty)
                if (item == null || empty) {
                    graphic = null
                    text = null
                }
                text = when {
                    empty -> ""
                    else -> playerName
                }

                val filter = model.readyPlayers.filter { it.value.value }
                if (filter.keys.contains(item)) {
                    style { backgroundColor += AppStyles.chosenCellColor }
                } else {
                    style { backgroundColor += Color.WHITE }
                }
            }
        }
    }
}