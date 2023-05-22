package org.home.mvc.contoller.serverTransfer

import home.extensions.AnysExtensions.invoke
import org.home.mvc.view.NewServerView
import tornadofx.label

class NewServerViewControllerForServer: NewServerViewController() {
    override fun NewServerView.subscriptions() { }

    override fun NewServerView.init() {
        battleController.setTurn(modelView.getNewServer())
        modelView {
            label("Вы новый сервер")
            val newServer = getNewServer()
            battleController.connect(newServer.ip, newServer.port)
        }
    }
}