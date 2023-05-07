package org.home.net

import javafx.application.Platform
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.mvc.contoller.AbstractGameController
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.model.BattleModel.Companion.invoke
import org.home.mvc.model.removeDestroyedDeck
import org.home.net.message.Action
import org.home.net.message.BattleEndAction
import org.home.net.message.DefeatAction
import org.home.net.message.FleetSettingsAction
import org.home.net.message.HitAction
import org.home.net.message.LeaveAction
import org.home.net.message.Message
import org.home.net.message.MissAction
import org.home.net.message.NotReadyAction
import org.home.net.message.Ping
import org.home.net.message.PlayerConnectionAction
import org.home.net.message.PlayerReadinessAction
import org.home.net.message.ReadyAction
import org.home.net.message.ShotAction
import org.home.net.message.event
import org.home.net.server.BattleEventEmitter
import org.home.utils.InfiniteTry.Companion.loop
import org.home.utils.InfiniteTryBase.Companion.doWhile
import org.home.utils.InfiniteTryBase.Companion.stopOnAll
import org.home.utils.SocketUtils.receive
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.AnysExtensions.name
import org.home.utils.extensions.AnysExtensions.plus
import org.home.utils.extensions.AtomicBooleansExtensions.atomic
import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.CollectionsExtensions.isEmpty
import org.home.utils.extensions.className
import org.home.utils.log
import org.home.utils.logReceive
import org.home.utils.singleThreadScope
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

class BattleClient: AbstractGameController(), BattleController<Action> {

    override val currentPlayer: String get() = super.currentPlayer

    private val battleEventEmitter: BattleEventEmitter by di()

    private val awaitConditions: AwaitConditions by newGame()

    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var serverSocket: Socket

    private val receiver = singleThreadScope(currentPlayer)
    private lateinit var receiverJob: Job
    private val canProceed = true.atomic

    class ActionTypeAbsentException(any: Any, className: String, method: String):
    RuntimeException("There is no when-branch for $any in $className#$method")

    override fun send(message: Message) = serverSocket { isNotClosed { send(message) } }
    override fun send(messages: Collection<Message>) = serverSocket { isNotClosed { send(messages) } }

    @Throws(UnknownHostException::class, IOException::class)
    override fun connect(ip: String, port: Int) {
        serverSocket = Socket(ip, port)
        output = serverSocket.getOutputStream()
        input = serverSocket.getInputStream()
        log { "connected to $ip:$port" }
        send(PlayerConnectionAction(currentPlayer))
        listen()
    }

    @Throws(IOException::class)
    fun listen() {
        canProceed(true)
        log { "client is listening for server ..." }
        receiverJob = receiver.launch {
            log { "receiver is launched" }

            loop {
                log { "waiting for message ..." }
                val messages = serverSocket.receive()
                logReceive(serverSocket, messages)
                messages.drop(1).forEach { process(it as Action) }
            } stopOnAll {
                SocketException::class + EOFException::class
            } doWhile canProceed
        }
        receiverJob.start()
    }

    private fun process(action: Action) {
        action.event?.also { fire(it); return }

        when (action) {
            is FleetSettingsAction -> {
                awaitConditions
                    .fleetSettingsReceived
                    .notifyUI { putSettings(action) }
            }

            is NotReadyAction -> processReadiness(action, ::PlayerIsNotReadyReceived)
            is ReadyAction -> processReadiness(action, ::PlayerIsReadyReceived)

            is ShotAction -> {
                val target = action.target
                if (target == currentPlayer) {
                    model
                        .registersAHit(action.shot)
                        .yes { onHit(action) }
                        .no {
                            MissAction(action).also {
                                send(it)
                                eventbus {
                                    +ThereWasAMiss(it)
                                }
                            }
                        }
                }
            }
            else -> throw ActionTypeAbsentException(action.className, this.className, "process")
        }
    }

    private fun onHit(shotAction: ShotAction) {
        val ships = model.shipsOf(currentPlayer)
        ships.removeDestroyedDeck(shotAction.shot)
        val hitAction = HitAction(shotAction)

        val defeatAction = DefeatAction(shotAction.player, currentPlayer)

        send {
            + hitAction
            ships.isEmpty { + defeatAction }
        }

        eventbus {
            + ShipWasHit(hitAction)
            ships.isEmpty {
                + PlayerWasDefeated(defeatAction)
            }
        }
    }

    override fun endBattle() {
        battleEventEmitter.endBattle()
    }


    override fun startBattle() {
        model.setReady(currentPlayer)
        send(ReadyAction(currentPlayer))
    }

    override fun leaveBattle() {
        hasConnection().so {
            send(LeaveAction(currentPlayer))
        }

        Platform.runLater {
            disconnect()
        }
    }

    private fun hasConnection() =
        try {
            send(Ping)
            true
        } catch (e: Exception) {
            false
        }

    override fun disconnect() {
        canProceed(false)
        runBlocking {
            receiverJob.cancel()
            log { "receiver's ${receiverJob.name } is canceled" }
        }
        input.close()
        output.close()
        serverSocket.close()
    }

    override fun onBattleViewExit() {
        leaveBattle()
    }

    override fun onWindowClose() {
        serverSocket.isNotClosed { leaveBattle() }
    }


    private fun processReadiness(action: PlayerReadinessAction, event: (PlayerReadinessAction) -> HasAPlayer) {
        action {
            model.setReady(player, isReady)
            eventbus { + event(this@action) }
        }
    }
}
