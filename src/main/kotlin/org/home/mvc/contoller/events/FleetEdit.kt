package org.home.mvc.contoller.events

import javafx.beans.property.SimpleIntegerProperty
import org.home.mvc.contoller.server.action.FleetEditAction

sealed class FleetEditEvent(player: String, val shipType: Int): HasAPlayer(player) {
    abstract val op: (Int) -> Int
    val propOp: SimpleIntegerProperty.() -> Unit = { value = op(value) }
    abstract val opSign: String
}

class ShipWasAdded(player: String, shipType: Int): FleetEditEvent(player, shipType) {
    constructor(action: FleetEditAction): this(action.player, action.shipType)
    override val op: (Int) -> Int = { it - 1 }
    override val opSign = "-"
}

class ShipWasDeleted(player: String, shipType: Int): FleetEditEvent(player, shipType) {
    constructor(action: FleetEditAction): this(action.player, action.shipType)
    override val op: (Int) -> Int = { it + 1 }
    override val opSign = "+"
}