package org.home.mvc.contoller.events

import org.home.net.FleetSettingsMessage
import tornadofx.FXEvent

class PlayerWasConnected(val playerName: String) : FXEvent()
class PlayerWasDisconnected(val playerName: String) : FXEvent()
class FleetSettingsAccepted(val settings: FleetSettingsMessage): FXEvent()

class PlayersAccepted(val players: Collection<String>): FXEvent()
class PlayerIsReadyAccepted(val player: String): FXEvent()
class ReadyPlayersAccepted(val players: Collection<String>): FXEvent()

class PlayerTurnToShoot(val player: String): FXEvent()
object WaitForYourTurn: FXEvent()