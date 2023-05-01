package org.home.mvc.view.battle.subscriptions

import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleStarted
import org.home.mvc.contoller.events.ConnectedPlayerReceived
import org.home.mvc.contoller.events.NewServerConnectionReceived
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.ShipWasConstructed
import org.home.mvc.contoller.events.ShipWasDeleted
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.allAreReady
import org.home.mvc.view.app.AppView
import org.home.mvc.view.NewServerView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.components.backSlide
import org.home.mvc.view.components.slide
import org.home.mvc.view.openMessageWindow
import org.home.mvc.view.updateFleetsReadiness
import org.home.net.action.NewServerConnectionAction
import org.home.style.AppStyles
import org.home.utils.IpUtils
import org.home.utils.extensions.CollectionsExtensions.excludeAll
import org.home.utils.log
import org.home.utils.logEvent
import tornadofx.action
import tornadofx.addClass
import tornadofx.hide

internal fun BattleView.playerWasConnected() {
    subscribe<ConnectedPlayerReceived> {
        logEvent(it, model)
        val connectedPlayer = it.player
        val ships = model.playersAndShips[connectedPlayer]
        if (ships == null) {
            model.playersAndShips[connectedPlayer] = mutableListOf()
            addSelectedPlayer(connectedPlayer)
            addEnemyFleetGrid(connectedPlayer)
            addEnemyFleetReadinessPane(connectedPlayer)
        }
    }
}


internal fun BattleView.playerIsReadyReceived() {
    subscribe<PlayerIsReadyReceived> {
        logEvent(it, model)

        if (model.allAreReady && appProps.isServer) {
            battleButton.isDisable = false
            battleButton.addClass(AppStyles.readyButton)
        }

        model.log { "ready = $playersReadiness" }
    }
}

internal fun BattleView.playerIsNotReadyReceived() {
    subscribe<PlayerIsNotReadyReceived> {
        logEvent(it, model)
        if (appProps.isServer) battleButton.updateStyle()
        model.log { "ready = $playersReadiness" }
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
                enemiesFleetsFleetGrids.excludeAll(defeatedPlayers).enable()
            } else {
                enemiesFleetsFleetGrids.disable()
            }
        }
    }
}


internal fun BattleView.shipWasConstructed() {
    subscribe<ShipWasConstructed> {
        logEvent(it, model)
        model.updateFleetsReadiness(it)
    }
}

internal fun BattleView.shipWasDeleted() {
    subscribe<ShipWasDeleted> {
        logEvent(it, model)
        model.updateFleetsReadiness(it)
    }
}

internal fun BattleView.battleIsStarted() {
    subscribe<BattleStarted> {
        logEvent(it, model)
        battleViewExitButton.text = "Покинуть бой"
        battleViewExitButton.action {
            battleController.leaveBattle()
            replaceWith(AppView::class, backSlide)
        }

        model {
            playersReadiness.forEach { (player, _) ->
                playersReadiness[player] = false
            }
        }

        battleButton.hide()
        openMessageWindow { "Бой начался" }
    }
}

internal fun BattleView.battleIsEnded() {
    subscribe<BattleIsEnded> {
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
            replaceWith(tornadofx.find(NewServerView::class), backSlide)
            battleController.disconnect()
            val freePort = IpUtils.freePort()
            val publicIp = IpUtils.publicIp()
            model.newServer = publicIp to freePort
            battleController.send(NewServerConnectionAction(currentPlayer, publicIp, freePort))
            appProps.isServer = true
        } else {
            replaceWith(tornadofx.find(NewServerView::class), backSlide)
        }
    }
}

internal fun NewServerView.serverTransferClientsReceived() {
    subscribe<NewServerConnectionReceived> {
        logEvent(it, model)
        battleController.disconnect()
        battleController.connectAndSend(it.action.ip, it.action.port)
        replaceWith(tornadofx.find(BattleView::class), slide)
    }
}