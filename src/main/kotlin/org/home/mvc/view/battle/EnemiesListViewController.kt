package org.home.mvc.view.battle

import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.yes
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import org.home.mvc.GameComponent
import org.home.mvc.model.BattleViewModel
import org.home.mvc.model.invoke
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
        view.itemsProperty().bindBidirectional(modelView.getEnemies());
        view.cellFactory =  cellFactory()
    }

    fun onSelect(body: (String, String?, String?) -> Unit) {
        view.selectionModel.selectedItemProperty().addListener { _, old, new ->
            log { "${EnemiesViewController::class.name} - old/new [$old/$new]" }
            body(currentPlayer, old, new)
            view.refresh()
        }
    }

    private fun cellFactory() = Callback<ListView<String>, ListCell<String>> {
        object : ListCell<String>() {
            init {
                addClass(emptyListCell)
            }

            override fun updateItem(playerName: String?, empty: Boolean) {
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

            fun BattleViewModel.setStyle(cell: ListCell<String>) {
                this {
                    when (cell.text) {
                        in getDefeatedPlayers() -> cell.toggle(defeatedListCell, rules)
                        in getReadyPlayers() -> cell.toggle(readyListCell, rules)
                        in getEnemies() -> cell.toggle(enemyListCell, rules)
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