package org.home.mvc.view.battle

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.ListView
import org.home.mvc.model.BattleModel
import org.home.style.AppStyles
import org.home.utils.log
import tornadofx.selectedItem

class EnemiesView(
    enemies: ObservableList<String>,
    model: BattleModel,
    val setEnemy: EnemiesView.(String) -> Unit
) : ListView<String>(enemies) {

    private val selectedEnemyLabel = Label()

    init {
        cellFactory = MarkReadyPlayers(model)
        changeEnemyFleetOnSelection(model.currentPlayer)
        id = AppStyles.playersListView
    }

    fun getSelectedEnemyLabel() = selectedEnemyLabel

    private fun ListView<String>.changeEnemyFleetOnSelection(currentPlayer: String) {
        selectionModel.selectedItemProperty().addListener { _, old, new ->
            select(currentPlayer, new, old)
        }
    }

    private fun select(currentPlayer: String, new: String?, old: String?) {
        if (new == currentPlayer) return
        val playerWasRemoved = new == null
        val selected = playerWasRemoved then {
            items.firstOrNull { it != old }
        } or {
            new
        }

        selected?.also {
            selectedEnemyLabel.textProperty().value = it
            setEnemy(it)
        }
        log { "selected: $selectedItem" }
    }
}