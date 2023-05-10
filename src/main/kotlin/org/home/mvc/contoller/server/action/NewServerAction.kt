package org.home.mvc.contoller.server.action

class NewServerAction(player: String) : PlayerAction(player)
class NewServerConnectionAction(player: String, val ip: String, val port: Int):
    PlayerAction(player)