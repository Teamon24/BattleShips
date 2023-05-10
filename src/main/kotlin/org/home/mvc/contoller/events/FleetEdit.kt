package org.home.mvc.contoller.events

import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.server.action.ShipAction

sealed class FleetEditEvent(val shipType: Int, player: String): HasAPlayer(player) {
    abstract val operation: SimpleIntegerProperty.() -> Unit
    abstract val op: String
}

class ShipWasAdded(shipType: Int, player: String): FleetEditEvent(shipType, player) {
    constructor(action: ShipAction): this(action.shipType, action.player)
    override val operation: SimpleIntegerProperty.() -> Unit = { value -= 1 }
    override val op = "-"
}

class ShipWasDeleted(shipType: Int, player: String): FleetEditEvent(shipType, player) {
    constructor(action: ShipAction): this(action.shipType, action.player)
    override val operation: SimpleIntegerProperty.() -> Unit = { value += 1 }
    override val op = "+"
}