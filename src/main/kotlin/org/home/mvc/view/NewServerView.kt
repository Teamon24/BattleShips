package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import home.extensions.BooleansExtensions.thus
import home.extensions.delete
import javafx.scene.Parent
import javafx.scene.layout.VBox
import org.home.app.di.gameScope
import org.home.app.di.noScope
import org.home.mvc.GameView
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.NewServerViewController
import org.home.mvc.contoller.events.BattleIsContinued
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.subscription.subscriptions
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.label
import tornadofx.runLater
import java.util.*
import kotlin.concurrent.thread


class NewServerView(override val root: Parent = VBox()) : GameView() {
    init {
        title = "${modelView.getCurrentPlayer().uppercase()}: перенос сервера"
    }

    internal val battleController by noScope<BattleController<Action>>()
    internal val newServerViewController by gameScope<NewServerViewController>()
    private var threadIndicator: Thread? = null
    private val connectedPlayers =
        Collections
            .synchronizedList(modelView.getPlayers().toMutableList())
            .apply { remove(modelView.getNewServer().player) }


    override fun onClose() {
        battleController.leaveBattle()
        battleController.disconnect()
    }

    init {
        subscriptions {
            serverTransferClientsReceived()
            playerWasConnected()
            playerLeaved()
            battleIsContinuedReceived()
        }

        applicationProperties.isServer thus {
            battleController.setTurn(modelView.getNewServer())
            modelView {
                label("Вы новый сервер")
                val newServer = getNewServer()
                battleController.connect(newServer.ip, newServer.port)
            }
        } otherwise {
            root {
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

    }

    private fun playerLeaved() {
        subscribe<PlayerLeaved> {
            TODO("${NewServerView::class.name}#subscribe<${PlayerLeaved::class.name}>")
            logEvent(it, modelView)
            connectedPlayers.remove(it.player)
            root { label("Отключился: ${it.player}") }
            connectedPlayers.isEmpty().so { }
        }
    }

    private fun playerWasConnected() {
        subscribe<ConnectedPlayerReceived> {
            logEvent(it, modelView)
            connectedPlayers.remove(it.player)
            root { label("Подключился: ${it.player}") }
            log { "connectedPlayers: $connectedPlayers" }
            connectedPlayers.isEmpty().so {
                battleController.continueBattle()
            }
        }
    }

    private fun battleIsContinuedReceived() {
        subscribe<BattleIsContinued> {
            logEvent(it, modelView)
            threadIndicator?.interrupt()
            viewSwitch {
                transferTo(BattleView::class) {
                    beforeTransfer {
                        subscriptionComponent { playerTurn(modelView.getNewServer().player) }
                    }
                }
            }
        }
    }

    private fun serverTransferClientsReceived() {
        subscribe<NewServerConnectionReceived> {
            logEvent(it, modelView)
            battleController.disconnect()
            log { "Отключение от предыдущего сервера..." }
            root { label("Отключение от предыдущего сервера...") }
            battleController.connect(it.action.ip, it.action.port)
            log { "Подключение к новому серверу..." }
            root { label("Подключение к новому серверу...") }
        }
    }
}
