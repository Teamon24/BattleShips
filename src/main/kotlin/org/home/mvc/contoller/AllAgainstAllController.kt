package org.home.mvc.contoller

import javafx.beans.property.SimpleBooleanProperty
import org.home.mvc.contoller.events.FleetSettingsAccepted
import org.home.mvc.contoller.events.PlayerIsReadyAccepted
import org.home.mvc.contoller.events.PlayerTurnToShoot
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.PlayersAccepted
import org.home.mvc.contoller.events.WaitForYourTurn
import org.home.mvc.model.BattleModel
import org.home.net.Action
import org.home.net.ConnectAction
import org.home.net.DefeatAction
import org.home.net.DisconnectAction
import org.home.net.EmptyAction
import org.home.net.EndGameAction
import org.home.net.FleetSettingsAction
import org.home.net.HitAction
import org.home.net.MessagesDSL.fleetSettings
import org.home.net.MessagesDSL.messages
import org.home.net.MessagesDSL.playersExcept
import org.home.net.MessagesDSL.readyPlayers
import org.home.net.MissAction
import org.home.net.PlayerSocket
import org.home.net.PlayersAction
import org.home.net.ReadyAction
import org.home.net.ShotAction
import org.home.net.TurnAction
import org.home.utils.SocketUtils.send
import org.home.utils.SocketUtils.sendAll
import org.home.utils.extensions.BooleansExtensions.or
import org.home.utils.extensions.BooleansExtensions.then
import org.home.utils.extensions.exclude
import org.home.utils.extensions.ln
import org.home.utils.log
import org.home.utils.logCom
import org.home.utils.logging

class AllAgainstAllController : GameTypeController() {
    override fun onShot(msg: ShotAction) {
        TODO("onShot")
    }
    override fun onHit(msg: HitAction) {
        fire(WaitForYourTurn)
    }

    override fun onEmpty(msg: EmptyAction) {
        TODO("onEmpty")
    }

    override fun onDefeat(msg: DefeatAction) {
        TODO("onDefeat")
    }

    override fun onDisconnect(msg: DisconnectAction) {
        TODO("onDisconnect")
    }

    override fun onEndGame(msg: EndGameAction) {
        TODO("onEndGame")
    }

    override fun onMiss(msg: MissAction) {
        TODO("onMiss")
    }

    override fun onConnect(
        socket: PlayerSocket,
        sockets: Collection<PlayerSocket>,
        connectAction: ConnectAction
    ) {
        val connectedPlayer = connectAction.player
        socket.player = connectedPlayer

        fire(PlayerWasConnected(connectedPlayer))


        logCom(connectedPlayer) {
            socket.send(
                messages {
                    fleetSettings(model)
                    playersExcept(connectedPlayer, model)
                    readyPlayers(model)
                }
            )
        }

        sockets
            .exclude(socket)
            .sendAll(connectAction)
    }

    override fun onMessage(msg: Action) {
        log { msg }
    }

    override fun onTurn(msg: TurnAction) {
        fire(PlayerTurnToShoot(msg.player))
    }

    override fun onReady(msg: ReadyAction): TurnAction? {
        model.readyPlayers[msg.player]!!.value = true
        fire(PlayerIsReadyAccepted(msg.player))

        return model.readyPlayers
            .all(::isReady)
            .and(model.playersNames.size == model.playersNumber.value)
            .then {
                chooseTurn(model)
                    .also { log {
                        "server chose ${it.player} to shot first"
                    } }
            } or {
                logging {
                    ln("ready players")
                    model.readyPlayers.forEach { (player, isReady) ->
                        ln("$player: ${isReady.value}")
                    }
                }
                null
            }
    }

    private fun isReady(it: Map.Entry<String, SimpleBooleanProperty>) = it.value.value

    override fun onFleetSettings(msg: FleetSettingsAction) {
        fire(FleetSettingsAccepted(msg))
    }

    override fun onPlayers(msg: PlayersAction) {
        fire(PlayersAccepted(msg.players))
    }

    override fun chooseTurn(model: BattleModel): TurnAction {
        val player = model.playersNames.toMutableList().run {
            shuffle()
            first()
        }
        return TurnAction(player)
    }
}




