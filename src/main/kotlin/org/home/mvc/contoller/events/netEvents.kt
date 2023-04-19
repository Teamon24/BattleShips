package org.home.mvc.contoller.events

import org.home.net.FleetSettingsMessage
import tornadofx.FXEvent

class PlayerWasConnected(val playerName: String) : FXEvent()
class PlayerWasDisconnected(val playerName: String) : FXEvent()
class FleetSettingsAccepted(val msg: FleetSettingsMessage): FXEvent()
class PlayersListAccepted(val players: List<String>): FXEvent()