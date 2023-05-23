package org.home.mvc.contoller.serverTransfer

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import home.extensions.delete
import javafx.scene.Parent
import javafx.scene.control.Label
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.view.NewServerView
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.add
import tornadofx.runLater
import kotlin.concurrent.thread

class NewServerViewControllerForClient: NewServerViewController() {
    override fun NewServerView.subscriptions() = serverTransferClientsReceived()
    private lateinit var serverTransferLabel: Label
    override fun NewServerView.initialize() {
        withRoot {
            threadIndicator = thread(name = "indicator") {
                labelMessage("Идет перенос сервера").also { serverTransferLabel = it }
                labelMessage("Отключение от предыдущего сервера...")
                labelMessage("Подключение к новому серверу...")
                while (connectedPlayers.isNotEmpty()) {
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        log { "connectedPlayers: $connectedPlayers" }
                        log { "indicating is stopped" }
                        break
                    }
                    runLater {
                        serverTransferLabel {
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
            it.action.newServer {
                battleController.connect(ip, port)
                modelView.getReadyPlayers().addAll(readyPlayers)
            }
        }
    }

    private fun Parent.labelMessage(message: String): Label {
        log { message }
        val label = Label(message)
        runLater {
            add(label)
        }
        return label
    }
}