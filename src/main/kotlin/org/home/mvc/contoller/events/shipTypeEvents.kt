package org.home.mvc.contoller.events

import tornadofx.FXEvent

class ShipCountEvent(val type: Int): FXEvent()
class ShipDiscountEvent(val type: Int): FXEvent()