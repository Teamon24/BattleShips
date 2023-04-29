package org.home.mvc.view.battle

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.currentPlayerListViewColors
import org.home.style.AppStyles.Companion.enemyListViewColors
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import tornadofx.style

class MarkReadyPlayers(val model: BattleModel) : Callback<ListView<String>, ListCell<String>> {
    override fun call(param: ListView<String>): ListCell<String> {
        return object : ListCell<String>() {

            override fun updateItem(playerName: String?, empty: Boolean) {

                super.updateItem(playerName, empty)

                if (item == null || empty) {
                    graphic = null
                    text = null
                }

                text = playerName

                    isDisable = (model.currentPlayer == text)
                        .yes { currentPlayerListViewColors.setStyle(model) }
                        .no { enemyListViewColors.setStyle(model) }
            }

            private fun AppStyles.PlayerListViewColors.setStyle(battleModel: BattleModel) {
                style {
                    textFill = when {
                        battleModel.turn.value == text -> turn
                        text in battleModel.defeatedPlayers -> defeated
                        text in battleModel.playersReadiness.thoseAreReady -> ready
                        else -> default
                    }
                }
            }
        }
    }
}