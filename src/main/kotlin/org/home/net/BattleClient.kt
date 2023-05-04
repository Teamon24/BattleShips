package org.home.net

import javafx.application.Platform
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.BattleController
import org.home.mvc.contoller.Conditions
import org.home.mvc.contoller.GameTypeController
import org.home.mvc.contoller.events.BattleIsEnded
import org.home.mvc.contoller.events.HasAPlayer
import org.home.mvc.contoller.events.PlayerIsNotReadyReceived
import org.home.mvc.contoller.events.PlayerIsReadyReceived
import org.home.mvc.contoller.events.ThereWasAMiss
import org.home.mvc.contoller.events.eventbus
import org.home.mvc.view.openMessageWindow
import org.home.net.action.Action
import org.home.net.action.BattleEndAction
import org.home.net.action.PlayerConnectionAction
import org.home.net.action.FleetSettingsAction
import org.home.net.action.LeaveAction
import org.home.net.action.MissAction
import org.home.net.action.NotReadyAction
import org.home.net.action.PlayerReadinessAction
import org.home.net.action.ReadyAction
import org.home.net.action.ShotAction
import org.home.net.action.event
import org.home.utils.SocketUtils.receive
import org.home.utils.SocketUtils.send
import org.home.utils.extensions.AnysExtensions.invoke
import org.home.utils.extensions.BooleansExtensions.no
import org.home.utils.extensions.BooleansExtensions.so
import org.home.utils.extensions.BooleansExtensions.yes
import org.home.utils.extensions.className
import org.home.utils.log
import org.home.utils.logReceive
import org.home.utils.singleThreadScope
import tornadofx.Scope
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

class BattleClient(applicationProperties: ApplicationProperties): BattleController(applicationProperties) {
    private val gameController: GameTypeController by di()
    private val conditions: Conditions by di()

    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var serverSocket: Socket

    private val receiver = singleThreadScope(currentPlayer)
    private lateinit var receiverJob: Job

    class ActionTypeAbsentException(any: Any, className: String, method: String):
    RuntimeException("There is no when-branch for $any in $className#$method")

    @Throws(UnknownHostException::class, IOException::class)
    fun connect(ip: String, port: Int) {
        serverSocket = Socket(ip, port)
        output = serverSocket.getOutputStream()
        input = serverSocket.getInputStream()
        log { "connected to $ip:$port" }
    }

    override fun connectAndSend(ip: String, port: Int) {
        connect(ip, port)
        send(PlayerConnectionAction(currentPlayer))
    }

    @Throws(IOException::class)
    fun listen() {
        log { "client is listening for server ..." }
        receiverJob = receiver.launch {
            log { "receiver is launched" }
            while (isActive) {
                log { "waiting for message ..." }
                try {
                    val messages = serverSocket.receive()
                    logReceive(serverSocket, messages)
                    messages.drop(1).forEach { process(it as Action)}
                } catch (e: SocketException) {
                    log { e.message }
                } catch (e: EOFException) {
                    log { e.message }
                }
            }
        }
        receiverJob.start()
    }

    private fun process(action: Action) {
        action.event?.also { fire(it); return }

        when (action) {
            is FleetSettingsAction -> {
                conditions
                    .fleetSettingsReceived
                    .notifyUI { putSettings(action) }
            }

            is NotReadyAction -> processReadiness(action, ::PlayerIsNotReadyReceived)
            is ReadyAction -> processReadiness(action, ::PlayerIsReadyReceived)
            is ShotAction -> {
                val target = action.target
                val serverIsTarget = target == currentPlayer
                if (serverIsTarget) {
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

    override fun send(action: Action) =
        serverSocket.isNotClosed.so { serverSocket.send(action) }

    override fun send(actions: Collection<Action>) =
        serverSocket.isNotClosed.so { serverSocket.send(actions) }

    override fun onBattleViewExit() {
        leaveBattle()
    }

    override fun onWindowClose() {
        serverSocket.isNotClosed.so { leaveBattle() }
    }

    override fun startBattle() {
        model.setReady(currentPlayer)
        send(ReadyAction(currentPlayer))
    }

    override fun leaveBattle() {
        send(LeaveAction(currentPlayer))
        Platform.runLater {
            disconnect()
        }
    }

    override fun endBattle() {
        eventbus {
            +BattleIsEnded(BattleEndAction(model.getWinner()))
        }
    }

    override fun disconnect() {
        input.close()
        output.close()
        serverSocket.close()
        runBlocking {
            receiver.cancel()
            receiverJob.cancel()
            log { "${this.javaClass }#receiver is canceled" }
        }
    }


    private fun processReadiness(action: PlayerReadinessAction, event: (PlayerReadinessAction) -> HasAPlayer) {
        action {
            model.playersReadiness[player] = isReady
            eventbus { + event(this@action) }
        }
    }
}
