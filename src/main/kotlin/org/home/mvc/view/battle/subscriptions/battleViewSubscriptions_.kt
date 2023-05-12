package org.home.mvc.view.battle.subscriptions

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
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
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.server.action.NewServerConnectionAction
import org.home.mvc.contoller.server.action.NotReadyAction
import org.home.mvc.contoller.server.action.ReadyAction
import org.home.mvc.contoller.server.action.ShipAction
import org.home.mvc.contoller.server.action.ShipAdditionAction
import org.home.mvc.contoller.server.action.ShipDeletionAction
import org.home.mvc.model.BattleModel
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.allAreReady
import org.home.mvc.view.NewServerView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.backSlide
import org.home.mvc.view.components.transferTo
import org.home.mvc.view.components.transitTo
import org.home.mvc.view.openMessageWindow
import org.home.style.AppStyles
import org.home.utils.IpUtils.freePort
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
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

private fun BattleView.addNewFleet(connectedPlayer: String) {
    model.playersAndShips[connectedPlayer] = mutableListOf()
    addSelectedPlayer(connectedPlayer)
    addEnemyFleetGrid(connectedPlayer)
    addEnemyFleetReadinessPane(connectedPlayer)
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


internal fun BattleView.playerIsReadyReceived() {
    subscribe<PlayerIsReadyReceived> {
        logEvent(it, model)

        if (model.allAreReady && applicationProperties.isServer) {
            battleStartButton.isDisable = false
            battleStartButton.addClass(AppStyles.readyButton)
        }

        model.log { "ready = $playersReadiness" }
    }
}

internal fun BattleView.playerIsNotReadyReceived() {
    also {
        it.subscribe<PlayerIsNotReadyReceived> { event ->
            logEvent(event, model)
            if (applicationProperties.isServer) {
                battleStartButton.updateStyle(it)
            }
            model.log { "ready = $playersReadiness" }
        }
    }
}

fun BattleView.readyPlayersReceived() {
    this.subscribe<ReadyPlayersReceived> { event ->
        logEvent(event, model)
        val playersReadiness = model.playersReadiness
        val players = event.players

        playersReadiness {
            when {
                isEmpty() -> putAll(players.associateWith { true })
                else -> players.forEach { player -> put(player, true) }
            }
        }
    }
}

internal fun BattleView.playerTurnToShoot() {
    subscribe<TurnReceived> { event ->
        model {
            logEvent(event, model)
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

internal fun BattleView.shipWasAdded() {
    subscribe<ShipWasAdded> {
        processFleetEdit(it) { shipType, player -> ShipAdditionAction(shipType, player) }
        if (it.player == currentPlayer) {
            if (model.hasAllShips(currentPlayer)) {
                model.setReady(it.player)
                battleController.send(ReadyAction(it.player))
            }
        }
    }
}

internal fun BattleView.shipWasDeleted() {
    subscribe<ShipWasDeleted> {
        processFleetEdit(it) { shipType, player -> ShipDeletionAction(shipType, player) }
        if (it.player == currentPlayer) {
            if (model.hasAllShips(currentPlayer)) {
                model.setNotReady(it.player)
                battleController.send(NotReadyAction(it.player))
            }
        }
    }
}

private fun BattleView.processFleetEdit(event: FleetEditEvent, action: (Int, String) -> ShipAction) {
    event {
        logEvent(event, model)
        if (event.player == currentPlayer) {
            battleController.send(action(event.shipType, currentPlayer))
        }
        model.updateFleetsReadiness(event)
    }
}

private fun BattleModel.updateFleetsReadiness(event: FleetEditEvent) {
    val operation = event.operation
    fleetsReadiness[event.player]!![event.shipType]!!.operation()
}

internal fun BattleView.battleIsStarted() {
    subscribe<BattleIsStarted> { event ->
        model.battleIsStarted = true
        logEvent(event, model)
        battleViewExitButton.text = leaveBattleText
        battleViewExitButton.action {
            battleController.leaveBattle()
            transitTo<AppView>(backSlide)
        }

        model {
            playersReadiness.forEach { (player, _) ->
                setNotReady(player)
            }
        }

        battleStartButton.hide()

        restoreCurrentPlayerFleetGrid()

        //НАЙТИ КАК УДАЛИТЬ EventHandler'ы у FleetGreed
        openMessageWindow { "Бой начался" }
    }
}

internal fun BattleView.battleIsEnded() {
    subscribe<BattleIsEnded> {
        logEvent(it, model)
        openMessageWindow {
            model.currentPlayerIs(it.player) then "Вы победили" or "Победил \"${it.player}\""
        }

        battleViewExitButton.text = leaveBattleFieldText
    }
}

data class NewServerInfo(val player: String, val ip: String, val port: Int)

internal fun BattleView.serverTransferReceived() {
    subscribe<NewServerReceived> { event ->
        logEvent(event, model)
        model {
            newServer = NewServerInfo(event.player, applicationProperties.ip, freePort())
            playersNumber.value -= 1
            currentPlayerIs(event.player).so {
                applicationProperties.isServer = true
                battleController {
                    send(NewServerConnectionAction(currentPlayer, newServer.ip, newServer.port))
                    disconnect()
                }
            }
        }
        transferTo<NewServerView>(backSlide)
    }
}
