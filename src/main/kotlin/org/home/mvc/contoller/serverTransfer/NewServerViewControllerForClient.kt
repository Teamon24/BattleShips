package org.home.mvc.contoller.serverTransfer

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import home.extensions.delete
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.view.NewServerView
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.label
import tornadofx.runLater
import kotlin.concurrent.thread

class NewServerViewControllerForClient: NewServerViewController() {
    override fun NewServerView.subscriptions() = serverTransferClientsReceived()

    override fun NewServerView.init() {
        withRoot {
            label("Идет перенос сервера") {
                threadIndicator = thread(name = "indicator") {
                    while (connectedPlayers.isNotEmpty()) {
                        try {
                            Thread.sleep(250)
                        } catch (e: InterruptedException) {
                            log { "connectedPlayers: $connectedPlayers" }
                            log { "indicating is stopped" }
                            break
                        }
                        runLater {
                            text = text.run { contains("...") then delete("...") or "$this." }
                        }
                    }
                }
            }
        }
    }

    private fun NewServerView.serverTransferClientsReceived() {
        subscribe<NewServerConnectionReceived> {
            logEvent(it, modelView)
            battleController.disconnect()
            log { "Отключение от предыдущего сервера..." }
            withRoot { label("Отключение от предыдущего сервера...") }
            battleController.connect(it.action.ip, it.action.port)
            log { "Подключение к новому серверу..." }
            withRoot { label("Подключение к новому серверу...") }
        }
    }
}