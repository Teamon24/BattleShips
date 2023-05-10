package org.home.mvc.contoller.server.action

sealed class ShipAction(val shipType: Int, player: String): PlayerAction(player) {
    abstract val op: String
}

class ShipDeletionAction(shipType: Int, player: String): ShipAction(shipType, player) {
    override val op get() = "-"
}

class ShipAdditionAction(shipType: Int, player: String): ShipAction(shipType, player) {
    override val op get() = "+"
}