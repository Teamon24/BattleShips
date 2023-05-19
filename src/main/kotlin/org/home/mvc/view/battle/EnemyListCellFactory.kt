package org.home.mvc.view.battle

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import org.home.mvc.model.BattleModel
import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.yes
import home.extensions.CollectionsExtensions.exclude
import org.home.style.AppStyles.Companion.defeatedListCell
import org.home.style.AppStyles.Companion.enemyListCell
import org.home.style.AppStyles.Companion.readyListCell
import org.home.style.StyleUtils.toggle
import tornadofx.CssRule
import tornadofx.addClass

class EnemyListCellFactory(val model: BattleModel) : Callback<ListView<String>, ListCell<String>> {
    private val rules = listOf(defeatedListCell, readyListCell, enemyListCell)

    override fun call(param: ListView<String>): ListCell<String> {
        return object : ListCell<String>() {
            init {
                addClass(enemyListCell)
            }
            override fun updateItem(playerName: String?, empty: Boolean) {
                super.updateItem(playerName, empty)

                if (item == null || empty) {
                    graphic = null
                    text = null
                }

                text = playerName

                isDisable = model.hasCurrent(text)
                    .yes { setStyle(model) }
                    .no { setStyle(model) }
            }

            private fun ListCell<String>.setStyle(model: BattleModel) {
                    model {
                        when {
                            text in defeatedPlayers -> enable(defeatedListCell)
                            text in readyPlayers -> enable(readyListCell)
                            else -> enable(enemyListCell)
                        }
                    }
            }

            private fun enable(cssRule: CssRule) {
                toggle(cssRule, rules.exclude(cssRule))
            }
        }
    }
}