package org.home.mvc.contoller.events

import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.server.action.FleetEditAction

sealed class FleetEditEvent(player: String, val shipType: Int): HasAPlayer(player) {
    abstract val operation: SimpleIntegerProperty.() -> Unit
    abstract val op: String
}

class ShipWasAdded(player: String, shipType: Int): FleetEditEvent(player, shipType) {
    constructor(action: FleetEditAction): this(action.player, action.shipType)
    override val operation: SimpleIntegerProperty.() -> Unit = { value -= 1 }
    override val op = "-"
}

class ShipWasDeleted(shipType: Int, player: String): FleetEditEvent(player, shipType) {
    constructor(action: FleetEditAction): this(action.shipType, action.player)
    override val operation: SimpleIntegerProperty.() -> Unit = { value += 1 }
    override val op = "+"
}