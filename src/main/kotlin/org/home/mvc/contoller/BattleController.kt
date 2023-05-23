package org.home.mvc.contoller

import org.home.mvc.model.Coord
import org.home.net.server.Message
import org.home.mvc.contoller.server.action.ShotAction
import org.home.mvc.view.battle.subscription.NewServerInfo
import org.home.utils.DSLContainer
import org.home.utils.dslContainer

interface BattleController<M: Message> {
    val currentPlayer: String

    fun send(message: Message)
    fun send(messages: Collection<Message>)
    fun send(addMessages: DSLContainer<Message>.() -> Unit) = send(dslContainer(addMessages))
    fun connect(ip: String, port: Int)

    fun shot(enemy: String, shot: Coord) = send(ShotAction(shot, currentPlayer, enemy))
    fun disconnect()

    fun startBattle()
    fun leaveBattle()
    fun endBattle()

    fun continueBattle()
}
