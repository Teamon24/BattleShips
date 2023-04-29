package org.home.mvc.contoller.events

import javafx.beans.property.SimpleIntegerProperty
import org.home.net.action.ShipConstructionAction
import org.home.net.action.ShipDeletionAction
import org.home.utils.RomansDigits
import tornadofx.FXEvent

sealed class FleetEditEvent(val shipType: Int, val player: String): FXEvent() {
    abstract val operation: SimpleIntegerProperty.() -> Unit
}

class ShipWasConstructed(shipType: Int, player: String): FleetEditEvent(shipType, player) {
    constructor(action: ShipConstructionAction): this(action.shipType, action.player)
    override val operation: SimpleIntegerProperty.() -> Unit = { value -= 1 }
    override fun toString() = "[${player}: -${RomansDigits.arabicToRoman(shipType)}]"
}

class ShipWasDeleted(shipType: Int, player: String): FleetEditEvent(shipType, player) {
    override val operation: SimpleIntegerProperty.() -> Unit = { value += 1 }
    constructor(action: ShipDeletionAction): this(action.shipType, action.player)
    override fun toString() = "[${player}: +${RomansDigits.arabicToRoman(shipType)}]"
}