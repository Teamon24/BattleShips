package org.home.mvc.view.battle

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.style.AppStyles
import org.home.style.AppStyles.Companion.currentPlayerListViewColors
import org.home.style.AppStyles.Companion.enemyListViewColors
import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.yes
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

                isDisable = model.currentPlayerIs(text)
                    .yes { currentPlayerListViewColors.setStyle(model) }
                    .no { enemyListViewColors.setStyle(model) }
            }

            private fun AppStyles.PlayerListViewColors.setStyle(model: BattleModel) {
                style {
                    model {
                        textFill = when {
                            turn.value == text -> turnColor
                            text in defeatedPlayers -> defeatedColor
                            text in thoseAreReady -> readyColor
                            else -> defaultColor
                        }
                    }
                }
            }
        }
    }
}