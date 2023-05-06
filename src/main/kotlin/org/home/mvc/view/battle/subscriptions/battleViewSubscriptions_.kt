package org.home.mvc.view.battle.subscriptions

import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleIsStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.ConnectedPlayersReceived
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.FleetsReadinessReceived
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.ReadyPlayersReceived
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.model.BattleModel
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.allAreReady
import org.home.mvc.view.NewServerView
import org.home.mvc.view.app.AppView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.backSlide
import org.home.mvc.view.components.forwardSlide
import org.home.mvc.view.components.transferTo
import org.home.mvc.view.components.transitTo
import org.home.mvc.view.fleet.FleetGrid
import org.home.mvc.view.openMessageWindow
import org.home.net.message.NewServerConnectionAction
import org.home.net.message.NotReadyAction
import org.home.net.message.ReadyAction
import org.home.net.message.ShipAction
import org.home.net.message.ShipAdditionAction
import org.home.net.message.ShipDeletionAction
import org.home.style.AppStyles
import org.home.utils.IpUtils
import org.home.utils.componentName
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.extensions.CollectionsExtensions.excludeAll
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.Scope
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.hide

fun View.subscriptions(subs: View.() -> Unit) {
    this.subs()
}

internal fun BattleView.playerWasConnected() {
    subscribe<ConnectedPlayerReceived> {
        logEvent(it, model)
        val connectedPlayer = it.player
        val ships = model.playersAndShips[connectedPlayer]
        if (ships == null) {
            addNewFleet(connectedPlayer)
        }
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
                get(player) ?: run {
                    put(player, mutableMapOf())
                }

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
            battleButton.isDisable = false
            battleButton.addClass(AppStyles.readyButton)
        }

        model.log { "ready = $playersReadiness" }
    }
}

internal fun BattleView.playerIsNotReadyReceived() {
    subscribe<PlayerIsNotReadyReceived> {
        logEvent(it, model)
        if (applicationProperties.isServer) battleButton.updateStyle()
        model.log { "ready = $playersReadiness" }
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
        battleViewExitButton.text = "Покинуть бой"
        battleViewExitButton.action {
            battleController.leaveBattle()
            transitTo<AppView>(backSlide)
        }

        model {
            playersReadiness.forEach { (player, _) ->
                setNotReady(player)
            }
        }

        battleButton.hide()

        restoreCurrentPlayerFleetGrid()

        //НАЙТИ КАК УДАЛИТЬ EventHandler'ы у FleetGreed
        fleetGridController.removeHandlers(currentPlayerFleetGridPane.center as FleetGrid)
        openMessageWindow { "Бой начался" }
    }
}

internal fun BattleView.battleIsEnded() {
    subscribe<BattleIsEnded> {
        model.battleIsEnded = true
        logEvent(it, model)
        openMessageWindow {
            if (it.player == currentPlayer) "Вы победили" else "Победил \"${it.player}\""
        }
        battleViewExitButton.text = "Покинуть поле боя"
    }
}


internal fun BattleView.serverTransferReceived() {
    subscribe<NewServerReceived> {
        logEvent(it, model)
        if (currentPlayer == it.player) {
            transitTo<NewServerView>(backSlide)
            battleController.disconnect()
            val freePort = IpUtils.freePort()
            val publicIp = IpUtils.publicIp()
            model.newServer = publicIp to freePort
            battleController.send(NewServerConnectionAction(currentPlayer, publicIp, freePort))
            applicationProperties.isServer = true
        } else {
            transitTo<NewServerView>(backSlide)
        }
    }
}

internal fun NewServerView.serverTransferClientsReceived() {
    subscribe<NewServerConnectionReceived> {
        logEvent(it, model)
        battleController.disconnect()
        battleController.connect(it.action.ip, it.action.port)
        transitTo<NewServerView>(backSlide)
    }
}


