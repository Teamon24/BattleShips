package org.home.mvc.contoller.server

import home.extensions.AnysExtensions.className
import home.extensions.AnysExtensions.name
import home.extensions.AnysExtensions.plus
import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.invoke
import home.extensions.BooleansExtensions.no
import home.extensions.BooleansExtensions.otherwise
import home.extensions.BooleansExtensions.so
import home.extensions.BooleansExtensions.thus
import home.extensions.BooleansExtensions.yes
import javafx.application.Platform
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.app.di.GameScope
import org.home.mvc.contoller.GameController
import org.home.mvc.contoller.AwaitConditions
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.events.BattleIsContinued
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.PlayerWasDefeated
import org.home.mvc.contoller.events.ShipWasHit
import org.home.mvc.contoller.events.ShipWasSunk
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.contoller.server.action.BattleContinuationAction
import org.home.mvc.contoller.server.action.ConnectionAction
import org.home.mvc.contoller.server.action.DefeatAction
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.FleetsReadinessAction
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.LeaveAction
import org.home.mvc.contoller.server.action.MissAction
import org.home.mvc.contoller.server.action.NotReadyAction
import org.home.mvc.contoller.server.action.ReadyAction
import org.home.mvc.contoller.server.action.ShotAction
import org.home.mvc.contoller.server.action.SinkingAction
import org.home.mvc.contoller.server.action.event
import org.home.mvc.model.areDestroyed
import org.home.mvc.model.isDestroyed
import org.home.mvc.model.removeAndGetBy
import org.home.net.server.Message
import org.home.net.server.Ping
import org.home.utils.InfiniteTry.Companion.loop
import org.home.utils.InfiniteTryBase.Companion.catch
import org.home.utils.InfiniteTryBase.Companion.doWhile
import org.home.utils.InfiniteTryBase.Companion.handle
import org.home.utils.InfiniteTryBase.Companion.stopOnAll
import org.home.utils.SocketUtils.isNotClosed
import org.home.utils.SocketUtils.receive
import org.home.utils.SocketUtils.send
import org.home.utils.log
import org.home.utils.logError
import org.home.utils.logReceive
import org.home.utils.singleThreadScope
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

class BattleClient : GameController(), BattleController<Action> {

    override val currentPlayer: String get() = super.currentPlayer

    private val battleEndingComponent: BattleEndingComponent by GameScope.inject()

    private val awaitConditions: AwaitConditions by GameScope.inject()

    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var serverSocket: Socket

    private val receiver = singleThreadScope(currentPlayer)
    private lateinit var receiverJob: Job
    private val canProceed = true.atomic

    class ActionTypeAbsentException(any: Any, className: String, method: String) :
        RuntimeException("There is no when-branch for $any in $className#$method")

    override fun send(message: Message) = serverSocket.isNotClosed { send(message) }
    override fun send(messages: Collection<Message>) = serverSocket.isNotClosed { send(messages) }

    @Throws(UnknownHostException::class, IOException::class)
    override fun connect(ip: String, port: Int) {
        serverSocket = Socket(ip, port)
        output = serverSocket.getOutputStream()
        input = serverSocket.getInputStream()
        log { "connected to $ip:$port" }
        send {
            +ConnectionAction(currentPlayer)
            modelView.hasReady(currentPlayer) {
                val fleetReadiness = modelView
                    .fleetsReadiness[currentPlayer]!!
                    .mapValues { it.value.value }

                +ReadyAction(currentPlayer)
                +FleetsReadinessAction(mapOf(currentPlayer to fleetReadiness))
            }
            listen()
        }
        awaitConditions.fleetSettingsReceived.await()
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
            } catch {
                +ClassCastException::class
                handle {
                    logError(it) { messages }
                }
            } doWhile canProceed
        }
        receiverJob.start()
    }

    private fun process(action: Action) {
        action.event?.also {
            eventbus(it)
            return
        }

        when (action) {
            is FleetSettingsAction -> awaitConditions.fleetSettingsReceived.notifyUI { putFleetSettings(action) }

            is NotReadyAction -> eventbus(PlayerIsNotReadyReceived(action))
            is ReadyAction -> eventbus(PlayerIsReadyReceived(action))

            is ShotAction -> {
                if (action.target == currentPlayer) {
                    modelView.registersAHit(action.shot)
                        .yes { onHit(action) }
                        .no { onMiss(action) }
                }
            }

            is BattleContinuationAction -> {
                awaitConditions.canContinueBattle.notifyUI()
                eventbus(BattleIsContinued)
            }

            else -> throw ActionTypeAbsentException(action.className, this.className, "process")
        }
    }

    private fun onMiss(action: ShotAction) {
        MissAction(action).also {
            send(it)
            eventbus(ThereWasAMiss(it))
        }
    }

    private fun onHit(shotAction: ShotAction) {
        val ships = modelView.shipsOf(currentPlayer)
        val hitShip = ships.removeAndGetBy(shotAction.shot)

        eventbus {
            send {
                hitShip.isDestroyed.thus {
                    SinkingAction(shotAction).also {
                        +it
                        +ShipWasSunk(it)
                    }
                } otherwise {
                    HitAction(shotAction).also {
                        +it
                        +ShipWasHit(it)
                    }
                }

                ships.areDestroyed {
                    DefeatAction(shotAction.player, currentPlayer).also {
                        +it
                        +PlayerWasDefeated(it)
                    }
                }
            }
        }
    }

    override fun endBattle() {
        battleEndingComponent.endBattle()
    }


    override fun startBattle() {
        modelView.setReady(currentPlayer)
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
            log { "receiver's ${receiverJob.name} is canceled" }
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

    override fun continueBattle() {
        awaitConditions.canContinueBattle.await()
    }
}
