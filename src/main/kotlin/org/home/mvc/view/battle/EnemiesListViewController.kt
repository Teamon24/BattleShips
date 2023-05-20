package org.home.mvc.view.battle

import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.yes
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import org.home.mvc.contoller.GameComponent
import org.home.mvc.model.BattleModel
import org.home.style.AppStyles.Companion.defeatedListCell
import org.home.style.AppStyles.Companion.emptyListCell
import org.home.style.AppStyles.Companion.enemyListCell
import org.home.style.AppStyles.Companion.readyListCell
import org.home.utils.StyleUtils.toggle
import org.home.utils.log
import tornadofx.addClass


class EnemiesListViewController: GameComponent() {
    private val rules = listOf(defeatedListCell, readyListCell, enemyListCell, emptyListCell)

    val view = ListView<String>()

    init {
        view.itemsProperty().bindBidirectional(modelView.enemies);
        view.cellFactory =  cellFactory()
    }

    fun onSelect(body: (String, String?, String?) -> Unit) {
        view.selectionModel.selectedItemProperty().addListener { _, old, new ->
            log { "${EnemiesViewController::class.name} - old/new [$old/$new]" }
            body(modelView.currentPlayer, old, new)
            view.refresh()
        }
    }

    private fun cellFactory() = Callback<ListView<String>, ListCell<String>> {
        object : ListCell<String>() {
            init {
                addClass(emptyListCell)
            }

            override fun updateItem(playerName: String?, empty: Boolean) {
                log { "enemies list view items on cell update - ${view.items.toMutableList()}" }
                super.updateItem(playerName, empty)

                if (item == null || empty) {
                    graphic = null
                    text = null
                }

                text = playerName

                isDisable = modelView.hasCurrent(text)
                    .yes { modelView.setStyle(this) }
                    .no { modelView.setStyle(this) }
            }

            fun BattleModel.setStyle(cell: ListCell<String>) {
                this {
                    when (cell.text) {
                        in defeatedPlayers -> cell.toggle(defeatedListCell, rules)
                        in readyPlayers -> cell.toggle(readyListCell, rules)
                        in enemies -> cell.toggle(enemyListCell, rules)
                        else -> cell.toggle(emptyListCell, rules)
                    }
                }
            }
        }
    }

    fun selectIfFirst(connectedPlayer: String, onFirst: (String) -> Unit) {
        (view.items.size == 1).so {
            view.selectionModel.select(connectedPlayer)
            onFirst(connectedPlayer)
        }
    }
}