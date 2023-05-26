package org.home.mvc.model

import home.extensions.AnysExtensions.invoke
import home.extensions.BooleansExtensions.yes
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import org.home.mvc.contoller.events.FleetEditEvent
import org.home.mvc.contoller.events.ShipWasAdded
import org.home.mvc.contoller.server.action.FleetSettingsAction
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.view.battle.subscription.NewServerInfo
import org.home.utils.extensions.ObservablePropertiesExtensions.copy
import org.home.utils.extensions.ObservablePropertiesExtensions.listProperty
import org.home.utils.extensions.ObservablePropertiesExtensions.mapProperty
import org.home.utils.extensions.ObservablePropertiesExtensions.setProperty
import org.home.utils.log
import org.home.utils.logOnChange

typealias PlayersAndShips = SimpleMapProperty<String, Ships>
typealias ShipsTypes = SimpleMapProperty<Int, Int>
typealias FleetReadiness = MutableMap<Int, SimpleIntegerProperty>
typealias FleetsReadiness = SimpleMapProperty<String, FleetReadiness>

class BattleViewModelImpl : BattleViewModel() {
    private val currentPlayer             = applicationProperties.currentPlayer
    private val size                      = applicationProperties.size
    private var maxShipType               = applicationProperties.maxShipType
    private val widthProp                 = SimpleIntegerProperty(size)
    private val heightProp                = SimpleIntegerProperty(size)
    private val playersNumberProp         = SimpleIntegerProperty(applicationProperties.playersNumber)
    private val width                     = bind { widthProp }.logOnChange("width")
    private val height                    = bind { heightProp }.logOnChange("height")
    private val playersNumber             = bind { playersNumberProp }.logOnChange("playersNumber")

    private val playersAndShips           = mapProperty<String, Ships>()
    private val shipsTypes                = mapProperty<Int, Int>()
    private val fleetsReadiness           = mapProperty<String, FleetReadiness>()
    private val players                   = listProperty<String>()
    private val enemies                   = listProperty<String>()
    private val defeatedPlayers           = listProperty<String>()
    private val readyPlayers              = setProperty<String>()
    private val statistics                = mutableListOf<HasAShot>()
    private var battleIsEnded             = false
    private var battleIsStarted           = false
    private var newServer: NewServerInfo? = null
    override val turn                     = SimpleStringProperty()

    init {
        fleetsReadiness.logOnChange("fleetsReadiness")
        shipsTypes.updateFleetReadiness().putInitials()
        players.logOnChange("players")
        enemies.logOnChange("enemies")
        defeatedPlayers.logOnChange("defeated")
        readyPlayers.logOnChange("ready players")
        playersAndShips.addOnChange().putInitials()
    }

    override fun add(player: String) { playersAndShips[player] = mutableListOf() }
    override fun remove(player: String) { playersAndShips.remove(player) }

    override fun getWidth(): SimpleIntegerProperty = width
    override fun getHeight(): SimpleIntegerProperty = height
    override fun getPlayersNumber(): SimpleIntegerProperty = playersNumber
    override fun getPlayersAndShipsNumber() = playersAndShips

    override fun getNewServerInfo() = newServer!!
    override fun newServerInfo(newServerInfo: NewServerInfo) {
        newServer = newServerInfo
    }

    override fun equalizeSizes() { getHeight().value = getWidth().value }

    override fun battleIsEnded(value: Boolean) { battleIsEnded = value.yes { battleIsStarted = false } }
    override fun battleIsStarted(value: Boolean) { battleIsStarted = value.yes { battleIsEnded = false } }
    override fun battleIsEnded() = battleIsEnded
    override fun battleIsStarted() = battleIsStarted
    override fun battleIsNotStarted() = !battleIsStarted

    //-------------------------------------------------------------------------------------------------
    //Fleets readiness
    override fun getFleetsReadiness(): FleetsReadiness = fleetsReadiness
    override fun setFleetReadiness(player: String, readiness: FleetReadiness) { fleetsReadiness[player] = readiness }
    override fun getFleetReadiness(player: String) = fleetsReadiness[player]!!
    override fun updateFleetReadiness(event: FleetEditEvent) {
        event { fleetsReadiness[player]!![shipType]!!.propOp() }
    }

    override fun putFleetSettings(settings: FleetSettingsAction) {
        getWidth().value = settings.width
        getHeight().value = settings.height
        shipsTypes.clear()
        shipsTypes.putAll(settings.shipsTypes)
    }

    //-------------------------------------------------------------------------------------------------
    //players
    override fun getCurrentPlayer() = currentPlayer
    override fun getPlayers(): SimpleListProperty<String> = players
    override fun getEnemies(): SimpleListProperty<String> = enemies
    override fun getDefeatedPlayers(): SimpleListProperty<String> = defeatedPlayers
    override fun getReadyPlayers(): SimpleSetProperty<String> = readyPlayers

    //-------------------------------------------------------------------------------------------------
    //shots
    override fun getHits() = getShots { it is HitAction }
    override fun getShotsAt(target: String) = getShots { it.target == target }
    override fun addShot(hasAShot: HasAShot) { statistics.add(hasAShot) }

    override fun getShots(function: (HasAShot) -> Boolean): Collection<Coord> =
        statistics.filter(function).map { it.shot }

    override fun hasNo(target: String, hitCoord: Coord): Boolean {
        return hitCoord !in statistics
            .asSequence()
            .filter { it.target == target }
            .map { it.shot }
    }

    //-------------------------------------------------------------------------------------------------
    //ships
    override fun String.ships() = playersAndShips[this]!!
    override fun String.decks() = playersAndShips[this]!!.flatten()
    override fun String.hasDeck(deck: Coord) = playersAndShips[this]!!.flatten().contains(deck)
    override fun shipsOf(player: String): Ships = playersAndShips[player]!!

    override fun getShipBy(hasAShot: HasAShot) = mutableListOf<Coord>().also { rightNext(hasAShot.shot, it) }
    override fun getShipsTypes() = shipsTypes

    override fun copyShipsTypes(): ShipsTypes {
        val copy = shipsTypes.copy()
        applicationProperties.ships?.let { ships ->
            ships.forEach {
                copy[it.size] = copy[it.size]!! - 1
            }
        }
        return copy
    }

    private fun SimpleMapProperty<Int, Int>.putInitials() = apply {
        0.until(maxShipType).forEach {
            put(it + 1, maxShipType - it)
        }
    }

    @JvmName("putInitialsToPlayersAndShips")
    private fun PlayersAndShips.putInitials(): PlayersAndShips {
        applicationProperties.ships?.let { ships ->
            set(currentPlayer, ships)
            log { "init ships from app props - $ships" }
            fleetsReadiness {
                val initialFleetReadiness = ships.fleetReadiness()
                if (initialFleetReadiness == shipsTypes) {
                    setReady(currentPlayer)
                }
                put(currentPlayer, initialFleetReadiness.toFleetsReadiness())
                ships.forEach { ship ->
                    updateFleetReadiness(ShipWasAdded(currentPlayer, ship.size))
                }
            }
        } ?: run {
            add(currentPlayer)
        }

        return this
    }

    override fun hasNoServerTransfer() = newServer == null

    private fun rightNext(
        coord: Coord,
        container: MutableList<Coord>
    ) {
        var tempCont = getHits().rightNextTo(coord)
        if (container.containsAll(tempCont)) return
        container.addAll(tempCont)

        tempCont.forEach {
            rightNext(it, container)
        }
    }
}

inline val BattleViewModel.allAreReady get() = getReadyPlayers().size == getPlayersNumber().value
//SimpleListProperty readyPlayer не сериализуется, поэтому - toMutableList
inline val BattleViewModel.thoseAreReady get() = getReadyPlayers().toMutableList()
