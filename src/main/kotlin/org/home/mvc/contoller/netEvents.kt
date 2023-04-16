package org.home.mvc.contoller

import tornadofx.FXEvent

class PlayerWasConnected(val playerName: String) : FXEvent()
class PlayerWasDisconnected(val playerName: String) : FXEvent()