package org.home.mvc.contoller

import org.home.app.ApplicationProperties
import org.home.mvc.contoller.BattleController.BattleControllerType.CLIENT
import org.home.mvc.contoller.BattleController.BattleControllerType.SERVER
import org.home.mvc.model.Coord
import org.home.net.server.Message
import org.home.mvc.contoller.server.action.ShotAction
import org.home.utils.DSLContainer
import org.home.utils.dslElements

interface BattleController<M: Message> {
    val currentPlayer: String
    val applicationProperties: ApplicationProperties

    enum class BattleControllerType {
        SERVER, CLIENT
    }

    fun send(message: Message)
    fun send(messages: Collection<Message>)
    fun send(addMessages: DSLContainer<Message>.() -> Unit) = send(dslElements(addMessages))
    fun connect(ip: String, port: Int)

    fun shot(enemy: String, shot: Coord) = send(ShotAction(shot, currentPlayer, enemy))
    fun disconnect()

    fun startBattle()
    fun leaveBattle()
    fun endBattle()

    fun continueBattle()
    fun switchTo(type: BattleControllerType) { applicationProperties.isServer = type == SERVER }
    fun switchToServer() { switchTo(SERVER) }
}
