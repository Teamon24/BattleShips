package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.thus
import javafx.scene.Parent
import org.home.app.di.FxScopes
import org.home.app.di.GameScope
import org.home.app.di.ViewInjector
import org.home.app.di.gameScope
import org.home.mvc.GameView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.BattleController.BattleControllerType
import org.home.mvc.contoller.BattleController.BattleControllerType.CLIENT
import org.home.mvc.contoller.BattleController.BattleControllerType.SERVER
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.battle.BattleView

class DebugView : GameView() {

    override lateinit var root: Parent
    val battleController by gameScope<BattleController<Action>>()

    init {
        viewSwitchButtonController {

            when (applicationProperties.player) {
                0 -> newGame(SERVER)
                else -> newGame(CLIENT)
            }

            ViewInjector {
                getView(BattleView::class, FxScopes.getGameScope()).also {
                    root = it.root
                }
            }
        }

        viewSwitchButtonController {
            val address = getIpPort()
            battleController {
                applicationProperties.isServer {
                    connect(address.first, address.second)
                } otherwise {
                    connect("", address.second)
                }
            }
        }
    }

    private fun newGame(isServer: BattleControllerType) {
        applicationProperties.isServer = isServer == SERVER
        GameScope.createNew()
    }


    override fun onClose() {  }
}
