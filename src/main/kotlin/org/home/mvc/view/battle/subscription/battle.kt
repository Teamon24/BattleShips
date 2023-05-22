package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import org.home.app.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.app.ApplicationProperties.Companion.leaveBattleText
import org.home.mvc.AppView
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.BattleIsStarted
import org.home.mvc.contoller.events.NewServerReceived
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.server.action.NewServerConnectionAction
import org.home.mvc.model.invoke
import org.home.mvc.view.NewServerView
import org.home.mvc.view.battle.BattleView
import org.home.mvc.view.openMessageWindow
import org.home.utils.IpUtils.freePort
import org.home.utils.NodeUtils.enable
import org.home.utils.logEvent
import tornadofx.View
import tornadofx.hide


inline fun View.subscriptions(subs: View.() -> Unit) {
    this.subs()
}

internal fun BattleView.subscribe() {
    subscriptions {
        playerWasConnected()
        connectedPlayersReceived()
        fleetsReadinessReceived()
        readyPlayersReceived()
        playerIsReadyReceived()
        playerIsNotReadyReceived()
        shipWasAdded()
        shipWasDeleted()
        battleIsStarted()
        playerTurnToShoot()
        shipWasHit()
        shipWasSunk()
        thereWasAMiss()
        playerLeaved()
        playerWasDefeated()
        playerWasDisconnected()
        battleIsEnded()
        serverTransferReceived()
    }
}

internal fun BattleView.playerTurnToShoot() {
    subscribe<TurnReceived> { event ->
        logEvent(event, modelView)
        subscriptionComponent { onPlayerTurn(event) }
    }
}

internal fun BattleView.battleIsStarted() {
    subscribe<BattleIsStarted> { event ->
        modelView.battleIsStarted(true)
        logEvent(event, modelView)
        battleViewExitButton.text = leaveBattleText

        viewSwitchButtonController.setTransit<AppView>(battleViewExitButton, this@battleIsStarted) {
            battleController.leaveBattle()
        }


        fleets().entries
            .zip(fleetsReadiness().entries) { fleet, readiness ->
                readinessStyleComponent {
                    notReady(fleet.key, fleet.value, readiness.value)
                }
            }

        modelView.getReadyPlayers().clear()
        battleStartButton.hide()

        currentFleetController.updateCurrentPlayerFleetGrid()

        //НАЙТИ КАК УДАЛИТЬ EventHandler'ы у FleetGreed
        openMessageWindow { "Бой начался" }
    }
}

internal fun BattleView.battleIsEnded() {
    subscribe<BattleIsEnded> { event ->
        logEvent(event, modelView)

        val player = event.player
        modelView.hasCurrent(player).so {
            currentFleetGridPane.enable()
            currentFleetReadinessPane.enable()
        }

        openMessageWindow {
            modelView.hasCurrent(player) then "Вы победили" or "Победил \"$player\""
        }

        battleViewExitButton.text = leaveBattleFieldText
    }
}

data class NewServerInfo(val player: String, val turnList: List<String>, val ip: String, val port: Int)

internal fun BattleView.serverTransferReceived() {
    subscribe<NewServerReceived> { event ->
        logEvent(event, modelView)
        modelView {
            event {
                val newServerInfo = NewServerInfo(player, turnList, applicationProperties.ip, freePort())
                setNewServer(newServerInfo)
                getPlayersNumber().value -= 1
                player.isCurrent {
                    battleController {
                        send(NewServerConnectionAction(getNewServer()))
                        disconnect()
                    }
                    applicationProperties.isServer = true
                }
            }
        }

        viewSwitch { backTransferTo(NewServerView::class) }
    }
}
