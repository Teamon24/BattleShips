package org.home.mvc.contoller.serverTransfer

import home.extensions.AnysExtensions.invoke
import home.extensions.AnysExtensions.name
import home.extensions.BooleansExtensions.so
import org.home.mvc.GameController
import org.home.mvc.contoller.events.BattleIsContinued
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.PlayerLeaved
import org.home.mvc.view.NewServerView
import org.home.mvc.view.battle.BattleView
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.label

sealed class NewServerViewController: GameController() {
    fun NewServerView.subscribe() {
        battleIsContinuedReceived()
        playerWasConnected()
        playerLeaved()
        subscriptions()
    }

    abstract fun NewServerView.subscriptions()

    abstract fun NewServerView.initialize()

    private fun NewServerView.battleIsContinuedReceived() {
        subscribe<BattleIsContinued> {
            logEvent(it, modelView)
            interruptIndicator()
            viewSwitch {
                transferTo(BattleView::class) {
                    beforeTransfer {
                        subscriptionComponent {
                            modelView {
                                battleIsStarted { onPlayerTurn(getNewServer().player) }
                                setAllReady(getNewServer().readyPlayers)
                            }

                            battleStartButtonController.updateStyle(currentPlayer)
                        }
                    }
                }
            }
        }
    }

    private fun NewServerView.playerLeaved() {
        subscribe<PlayerLeaved> {
            TODO("${NewServerView::class.name}#subscribe<${it::class.name}>")
            logEvent(it, modelView)
            connectedPlayers.remove(it.player)
            root { label("Отключился: ${it.player}") }
            connectedPlayers.isEmpty().so { }
        }
    }

    private fun NewServerView.playerWasConnected() {
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
}

