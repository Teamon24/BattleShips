package org.home.mvc.contoller

import org.home.mvc.GameController
import org.home.mvc.view.NewServerView

sealed class NewServerViewController: GameController() {
    abstract fun NewServerView.subscribe()
}

class NewServerViewControllerForServer: NewServerViewController() {
    override fun NewServerView.subscribe() {
        TODO("Not yet implemented")
    }
}

class NewServerViewControllerForClient: NewServerViewController() {
    override fun NewServerView.subscribe() {
        TODO("Not yet implemented")
    }
}