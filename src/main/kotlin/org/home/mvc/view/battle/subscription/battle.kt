package org.home.mvc.view.battle.subscription

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.then
import org.home.app.ApplicationProperties.Companion.leaveBattleFieldText
import org.home.app.ApplicationProperties.Companion.leaveBattleText
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
import org.home.utils.logEvent
import tornadofx.View
import java.io.Serializable


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
        battleViewExitButtonController.setText(leaveBattleText)
        battleViewExitButtonController.setTransit(this@battleIsStarted)

        fleets().entries
            .zip(fleetsReadiness().entries) { fleet, readiness ->
                readinessStyleComponent {
                    notReady(fleet.key, fleet.value, readiness.value)
                }
            }

        modelView.getReadyPlayers().clear()
        battleStartButtonController.hide()
        currentFleetController.updateCurrentPlayerFleetGrid()
        openMessageWindow { "Бой начался" }
    }
}

internal fun BattleView.battleIsEnded() {
    subscribe<BattleIsEnded> { event ->
        logEvent(event, modelView)

        val player = event.player
        modelView.hasCurrent(player).so {
            currentFleetController.enableView()
        }

        openMessageWindow {
            modelView.hasCurrent(player) then "Вы победили" or "Победил \"$player\""
        }

        battleViewExitButtonController.setText(leaveBattleFieldText)
    }
}

class NewServerInfo: Serializable {
    lateinit var ip: String
    var port: Int = 0
    lateinit var player: String
    lateinit var turnList: List<String>
    lateinit var readyPlayers: Set<String>
    override fun toString(): String {
        return "${this.className}([$player][$ip:$port] /turn=$turnList /ready=$readyPlayers)"
    }
}

internal fun BattleView.serverTransferReceived() {
    subscribe<NewServerReceived> { event ->
        logEvent(event, modelView)
        val newServerPlayer = event.player
        modelView {
            newServer {
                ip = addressComponent.publicIp()
                port = addressComponent.freePort()
                player = newServerPlayer
                player = newServerPlayer
                turnList = event.action.turnList
                readyPlayers = event.action.readyPlayers
            }

            getPlayersNumber().value -= 1
            newServerPlayer.isCurrent {
                battleController {
                    applicationProperties.isServer = true
                    send(NewServerConnectionAction(getNewServerInfo()))
                    disconnect()
                }
            }
        }

        viewSwitch { backTransferTo(NewServerView::class) }
    }
}
