package org.home.mvc.view.battle.subscriptions

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import home.extensions.CollectionsExtensions.excludeAll
import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.AppView
import org.home.mvc.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.mvc.ApplicationProperties.Companion.leaveBattleText
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleIsStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.server.action.NewServerConnectionAction
import org.home.mvc.view.NewServerView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.Transit.BACKWARD
import org.home.mvc.view.components.transferTo
import org.home.mvc.view.components.transitTo
import org.home.mvc.view.openMessageWindow
import org.home.utils.IpUtils.freePort
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.View
import tornadofx.action
import tornadofx.hide


inline fun View.subscriptions(subs: View.() -> Unit) {
    this.subs()
}

internal fun BattleView.playerWasConnected() {
    subscribe<ConnectedPlayerReceived> {
        logEvent(it, model)
        addNewFleet(it.player)
    }
}

fun BattleView.connectedPlayersReceived() {
    subscribe<ConnectedPlayersReceived> { event ->
        logEvent(event, model)
        event.players.forEach { connectedPlayer ->
            addNewFleet(connectedPlayer)
        }
    }
}

internal fun BattleView.fleetsReadinessReceived() {
    subscribe<FleetsReadinessReceived> { event ->
        logEvent(event, model)
        model.fleetsReadiness {
            event.states.forEach { (player, state) ->
                get(player) ?: run { put(player, mutableMapOf()) }

                state.forEach { (shipType, number) ->
                    val shipNumberProperty = get(player)!![shipType]
                    shipNumberProperty ?: run {
                        get(player)!![shipType] = SimpleIntegerProperty(number)
                        return@fleetsReadiness
                    }

                    shipNumberProperty.value = number
                }
            }
        }
    }
}

internal fun BattleView.playerTurnToShoot() {
    subscribe<TurnReceived> { event ->
        logEvent(event, model)
        model {
            turn.value = event.player
            if (currentPlayer == event.player) {
                openMessageWindow { "Ваш ход" }
                log { "defeated = $defeatedPlayers" }
                enemiesFleetGridsPanes.excludeAll(defeatedPlayers).enable()
            } else {
                enemiesFleetGridsPanes.disable()
            }
        }
    }
}

internal fun BattleView.battleIsStarted() {
    subscribe<BattleIsStarted> { event ->
        model.battleIsStarted = true
        logEvent(event, model)
        battleViewExitButton.text = leaveBattleText

        battleViewExitButton.action {
            battleController.leaveBattle()
            transitTo<AppView>(BACKWARD)
        }

        model.readyPlayers.clear()

        battleStartButton.hide()

        updateCurrentPlayerFleetGrid()

        //НАЙТИ КАК УДАЛИТЬ EventHandler'ы у FleetGreed
        openMessageWindow { "Бой начался" }
    }
}

internal fun BattleView.battleIsEnded() {
    subscribe<BattleIsEnded> { event ->
        logEvent(event, model)

        openMessageWindow {
            val player = event.player
            model.hasCurrent(player) then "Вы победили" or "Победил \"$player\""
        }

        battleViewExitButton.text = leaveBattleFieldText
    }
}

data class NewServerInfo(val player: String, val ip: String, val port: Int)

internal fun BattleView.serverTransferReceived() {
    subscribe<NewServerReceived> { event ->
        logEvent(event, model)
        model {
            event {
                newServer = NewServerInfo(player, applicationProperties.ip, freePort())
                playersNumber.value -= 1
                player.isCurrent {
                    applicationProperties.isServer = true
                    battleController {
                        send(NewServerConnectionAction(newServer))
                        disconnect()
                    }
                }
            }
        }
        transferTo<NewServerView>(BACKWARD)
    }
}
