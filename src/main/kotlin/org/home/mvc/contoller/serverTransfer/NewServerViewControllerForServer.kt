package org.home.mvc.contoller.serverTransfer

import home.extensions.AnysExtensions.invoke
import home.extensions.CollectionsExtensions.isNotEmpty
import org.home.mvc.view.NewServerView
import tornadofx.label

class NewServerViewControllerForServer: NewServerViewController() {
    override fun NewServerView.subscriptions() { }

    override fun NewServerView.initialize() {
        modelView.getNewServer().apply {
            turnList.isNotEmpty {
                playerTurnComponent.turnList = turnList
                playerTurnComponent.turnPlayer = player
            }
        }

        modelView {
            withRoot { label("Вы новый сервер") }
            val newServer = getNewServer()
            battleController.connect(newServer.ip, newServer.port)
        }
    }
}