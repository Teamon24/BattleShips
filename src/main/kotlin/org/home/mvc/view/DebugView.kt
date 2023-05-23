package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.thus
import javafx.scene.Parent
import org.home.app.di.FxScopes
import org.home.app.di.ViewInjector
import org.home.app.di.gameScope
import org.home.mvc.GameView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.battle.BattleView

class DebugView : GameView() {

    override lateinit var root: Parent
    val battleController by gameScope<BattleController<Action>>()

    init {
        viewSwitchButtonController {
            setServerNewGame(applicationProperties.player == 0)
            ViewInjector {
                getView(BattleView::class, FxScopes.getGameScope()).also {
                    root = it.root
                    it.battleViewExitButton
                }
            }
        }

        viewSwitchButtonController {
            val address = getIpPort()
            applicationProperties {
                isServer.thus {
                    battleController.connect(address.first, address.second)
                }.otherwise {
                    battleController.connect("", address.second)
                }
            }
        }

    }


    override fun onClose() {  }
}
