package org.home.net.server

import home.extensions.BooleansExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.yes
import home.extensions.CollectionsExtensions.hasElements
import home.extensions.CollectionsExtensions.isEmpty
import org.home.mvc.contoller.AbstractGameBean
import org.home.mvc.contoller.ShotNotifierStrategies
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.TurnReceived
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.areHit
import org.home.mvc.model.removeDestroyedDeck
import org.home.net.PlayerSocket
import org.home.net.message.DefeatAction
import org.home.net.message.HasAShot
import org.home.net.message.HitAction
import org.home.net.message.MissAction
import org.home.net.message.ShotAction
import org.home.net.message.TurnAction
import org.home.utils.PlayerSocketUtils.send
import org.home.utils.PlayersSocketsExtensions.get
import org.home.utils.SocketUtils.send


class ShotProcessingComponent: AbstractGameBean() {

    private val notifierStrategies: ShotNotifierStrategies by di()
    private val multiServerSockets: MultiServer.MultiServerSockets<PlayerSocket> by di()
    private val playerTurnComponent: PlayerTurnComponent by di()

    private val sockets = multiServerSockets.get()
    private val shotNotifier = notifierStrategies.create(sockets)

    internal val turnList = playerTurnComponent.turnList
    private inline val <E> Collection<E>.hasPlayers get() = hasElements

    internal fun socket(player: String) = sockets[player]

    fun onShot(action: ShotAction) {
        val target = action.target
        if (target == currentPlayer) {
            val ships = model.shipsOf(target)
            val shot = action.shot
            ships.areHit(shot).yes {
                onHitShot(action.hit())
                ships.removeDestroyedDeck(shot)
                ships.isEmpty.invoke {
                    playerTurnComponent.remove(target)
                    val shooter = action.player
                    val defeatAction = DefeatAction(shooter, target)

                    sockets.send(defeatAction)

                    turnList.hasPlayers {
                        socket(shooter).send(TurnAction(shooter))
                    }
                    eventbus { +PlayerWasDefeated(defeatAction) }
                }
            } no {
                onMissedShot(action)
            }
        } else {
            sockets.send(shotNotifier.messages(action))
            socket(target).send(action)
        }
    }

    fun onMiss(action: MissAction) {
        val nextTurn = notifyAboutShotAndSendNextTurn(action)

        eventbus {
            +ThereWasAMiss(action)
            +TurnReceived(TurnAction(nextTurn))
        }
    }


    private fun onMissedShot(action: ShotAction) {
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

    fun onHitShot(hitAction: HitAction) {
        val notifications = shotNotifier.messages(hitAction)
        sockets.send(notifications)
        eventbus {
            +ShipWasHit(hitAction)
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