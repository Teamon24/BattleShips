package org.home.mvc.view

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import home.extensions.BooleansExtensions.yes
import home.extensions.delete
import javafx.scene.Parent
import javafx.scene.layout.VBox
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.BattleIsContinued
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.battle.subscription.subscriptions
import org.home.mvc.view.component.transferTo
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.label
import tornadofx.runLater
import java.util.*
import kotlin.concurrent.thread


class NewServerView(override val root: Parent = VBox()) : AbstractGameView() {
    private val battleController: BattleController<Action> by di()
    private var threadIndicator: Thread? = null
    private val connectedPlayers =
        Collections
            .synchronizedList(model.players.toMutableList())
            .apply { remove(model.newServer.player) }


    override fun exit() {
        battleController.leaveBattle()
        battleController.disconnect()
        super.exit()
    }

    init {
        title = "${model.currentPlayer.uppercase()}: перенос сервера"
        root {
            applicationProperties.isServer.yes {
                model {
                    label("Вы новый сервер")
                    battleController.connect(newServer.ip, newServer.port)
                }
            } no {
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

        subscriptions {
            serverTransferClientsReceived()
            subscribe<ConnectedPlayerReceived> {
                logEvent(it, model)
                connectedPlayers.remove(it.player)
                root { label("Подключился: ${it.player}") }
                log { "connectedPlayers: $connectedPlayers" }
                connectedPlayers.isEmpty().so {
                    battleController.continueBattle()
                }
            }

            subscribe<PlayerLeaved> {
                TODO("${NewServerView::class.name}#subscribe<${PlayerLeaved::class.name}>")
                logEvent(it, model)
                connectedPlayers.remove(it.player)
                root { label("Отключился: ${it.player}") }
                connectedPlayers.isEmpty().so { }
            }
            battleIsContinuedReceived()
        }
    }

    private fun battleIsContinuedReceived() {
        subscribe<BattleIsContinued> {
            threadIndicator?.interrupt()
            logEvent(it, model)
            transferTo<BattleView>()
        }
    }

    private fun serverTransferClientsReceived() {
        subscribe<NewServerConnectionReceived> {
            logEvent(it, model)
            battleController.disconnect()
            log { "Отключение от предыдущего сервера..." }
            root { label("Отключение от предыдущего сервера...") }
            battleController.connect(it.action.ip, it.action.port)
            log { "Подключение к новому серверу..." }
            root { label("Подключение к новому серверу...") }
        }
    }
}
