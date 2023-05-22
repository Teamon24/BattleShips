package org.home.mvc.contoller.server

import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.thus
import home.extensions.CollectionsExtensions.hasElements
import home.extensions.CollectionsExtensions.isEmpty
import org.home.app.di.noScope
import org.home.mvc.GameComponent
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ShipWasSunk
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.contoller.server.PlayersSocketsExtensions.get
import org.home.mvc.contoller.server.action.DefeatAction
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.MissAction
import org.home.mvc.contoller.server.action.ShotAction
import org.home.mvc.contoller.server.action.SinkingAction
import org.home.mvc.contoller.server.action.TurnAction
import org.home.mvc.model.aintHit
import org.home.mvc.model.removeAndGetBy
import org.home.net.server.MultiServer.MultiServerSockets
import org.home.utils.PlayerSocketUtils.send
import org.home.utils.SocketUtils.send

class ShotProcessingComponent: GameComponent() {
    private val playerTurnComponent by noScope<PlayerTurnComponent>()

    private val multiServerSockets by noScope<MultiServerSockets<PlayerSocket>>()
    private val sockets = multiServerSockets.get()

    private val notifierStrategies by noScope<ShotNotifierStrategies>()
    private val shotNotifier = notifierStrategies.create(modelView.getEnemies())

    private val turnList = playerTurnComponent.turnList
    private inline val <E> Collection<E>.hasPlayers get() = hasElements

    internal fun socket(player: String) = sockets[player]

    fun onShot(action: ShotAction) {
        val target = action.target
        if (target == currentPlayer) {
            onShotAtServer(target, action)
        } else {
            socket(target).send(action)
        }
    }

    private fun onShotAtServer(target: String, action: ShotAction) {
        val ships = modelView.shipsOf(target)
        val shot = action.shot

        shot.aintHit(ships)
            .thus { onMiss(action) }
            .otherwise {
                val hitShip = ships.removeAndGetBy(shot)

                when(hitShip.isEmpty) {
                    true -> onSinking(SinkingAction(action))
                    else -> onHit(HitAction(action))
                }

                ships.isEmpty {
                    playerTurnComponent.remove(target)
                    val shooter = action.player
                    val defeatAction = DefeatAction(shooter, target)

                    sockets.send(defeatAction)

                    turnList.hasPlayers {
                        socket(shooter).send(TurnAction(shooter))
                    }

                    eventbus(PlayerWasDefeated(defeatAction) )
                    return@onShotAtServer
                }
            }
    }

    fun onSinking(sinkingAction: SinkingAction) {
        val notifications = shotNotifier.messages(sinkingAction)
        sockets.send(notifications)
        eventbus {
            +ShipWasSunk(sinkingAction)
        }
    }

    fun onHit(hitAction: HitAction) {
        val notifications = shotNotifier.messages(hitAction)
        sockets.send(notifications)
        eventbus {
            +ShipWasHit(hitAction)
        }
    }


    fun onMiss(action: MissAction) {
        val nextTurn = notifyAboutShotAndSendNextTurn(action)

        eventbus {
            +ThereWasAMiss(action)
            +TurnReceived(TurnAction(nextTurn))
        }
    }

    private fun onMiss(action: ShotAction) {
        val missAction = MissAction(action)
        val nextTurn = playerTurnComponent.nextTurn()

        sockets.send {
            +missAction
            +TurnAction(nextTurn)
        }

        eventbus {
            +ThereWasAMiss(missAction)
            +TurnReceived(TurnAction(nextTurn))
        }
    }

    private fun notifyAboutShotAndSendNextTurn(action: HasAShot): String {
        val playersAndShotMessages = shotNotifier.messages(action)

        val nextTurn = playerTurnComponent.nextTurn()
        val playersAndTurnAction = sockets.associate {
            it.player!! to mutableListOf(TurnAction(nextTurn))
        }

        sockets.send(playersAndShotMessages, playersAndTurnAction)

        return nextTurn
    }
}