package org.home.mvc.contoller

import javafx.beans.property.SimpleBooleanProperty
import org.home.mvc.contoller.events.FleetSettingsAccepted
import org.home.mvc.contoller.events.PlayerIsReadyAccepted
import org.home.mvc.contoller.events.PlayerTurnToShoot
import org.home.mvc.contoller.events.PlayerWasConnected
import org.home.mvc.contoller.events.PlayersAccepted
import org.home.mvc.contoller.events.WaitForYourTurn
import org.home.mvc.model.BattleModel
import org.home.mvc.model.thoseAreReady
import org.home.net.ConnectMessage
import org.home.net.DefeatMessage
import org.home.net.DisconnectMessage
import org.home.net.EmptyMessage
import org.home.net.EndGameMessage
import org.home.net.HitMessage
import org.home.net.ActionMessage
import org.home.net.FleetSettingsMessage
import org.home.net.Message
import org.home.net.MissMessage
import org.home.net.PlayersMessage
import org.home.net.ReadyMessage
import org.home.net.ReadyPlayersMessage
import org.home.net.ShotMessage
import org.home.net.TurnMessage
import org.home.utils.SocketUtils.send
import org.home.utils.SocketUtils.sendAll
import org.home.utils.SocketUtils.sendAndReceive
import org.home.utils.functions.exclude
import org.home.utils.ln
import org.home.utils.log
import org.home.utils.logCom
import org.home.utils.logging
import org.home.utils.functions.or
import org.home.utils.functions.then
import java.net.Socket

class AllAgainstAllController : GameTypeController() {
    override fun onShot(msg: ShotMessage): ActionMessage {
        TODO("onShot")
    }
    override fun onHit(msg: HitMessage) {
        fire(WaitForYourTurn)
    }

    override fun onEmpty(msg: EmptyMessage): ActionMessage {
        TODO("onEmpty")
    }

    override fun onDefeat(msg: DefeatMessage): ActionMessage {
        TODO("onDefeat")
    }

    override fun onDisconnect(msg: DisconnectMessage): ActionMessage {
        TODO("onDisconnect")
    }

    override fun onEndGame(msg: EndGameMessage): ActionMessage {
        TODO("onEndGame")
    }

    override fun onMiss(msg: MissMessage): ActionMessage {
        TODO("onMiss")
    }

    override fun onConnect(
        sockets: MutableMap<String, Socket>,
        msg: ConnectMessage
    ) {
        val connectedPlayer = msg.player

        val socket = sockets[connectedPlayer]!!

        val out = socket.getOutputStream()
        val `in` = socket.getInputStream()

        fire(PlayerWasConnected(connectedPlayer))

        logCom(connectedPlayer) {
            out.sendAndReceive(FleetSettingsMessage(model), `in`)
        }

        logCom(connectedPlayer) {
            out.send(PlayersMessage(model.playersNames.exclude(connectedPlayer)))
        }

        logCom(connectedPlayer) {
            out.send(ReadyPlayersMessage(model.readyPlayers.thoseAreReady))
        }

        sockets
            .exclude(connectedPlayer)
            .sendAll(msg)
    }

    override fun onMessage(msg: Message) {
        log { msg }
    }

    override fun onTurn(msg: TurnMessage) {
        fire(PlayerTurnToShoot(msg.player))
    }

    override fun onReady(msg: ReadyMessage): TurnMessage? {
        model.readyPlayers[msg.player]!!.value = true
        fire(PlayerIsReadyAccepted(msg.player))

        return model.readyPlayers
            .all(::isReady)
            .and(model.playersNames.size == model.playersNumber.value)
            .then {
                chooseTurn(model).also {
                    log { "server chose ${it.player} to shot first" }
                }
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

    override fun onFleetSettings(msg: FleetSettingsMessage) {
        fire(FleetSettingsAccepted(msg))
    }

    override fun onPlayers(msg: PlayersMessage) {
        fire(PlayersAccepted(msg.players))
    }

    override fun chooseTurn(model: BattleModel): TurnMessage {
        val player = model.playersNames.toMutableList().run {
            shuffle()
            first()
        }
        return TurnMessage(player)
    }
}




