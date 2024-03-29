package org.home.app

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import home.extensions.BooleansExtensions.so
import org.apache.commons.lang3.StringUtils
import org.home.mvc.contoller.ShipsPane
import org.home.mvc.model.Ships
import org.home.mvc.model.copy
import org.home.mvc.model.ship
import org.home.mvc.view.component.ViewSwitch
import org.home.utils.extensions.StringBuildersExtensions.ln
import org.home.utils.logEach
import org.home.utils.logging
import java.util.*

class ApplicationProperties(private val appPropsFileName: String = "current-player") {

    private val Any?.asString get() = this as String?
    private val Any?.asInt get() = asString?.toInt()
    private val Any?.asBool get() = asString?.toBoolean()
    private val props = Properties().apply {
        put(gameTypeProperty, "")
    }

    init {
        try {
            Companion::class.java
                .getResourceAsStream("/${appPropsFileName}.properties")
                .use { stream -> props.load(stream) }

            props["ships"]
                ?.let { prop ->
                    Json.parse(prop as String)
                        .asArray()
                        .map { ships ->
                            ships.asCollection().ship { it.asCoord() } } }
                ?.let { ships ->
                    props["ships"] = ships
                    props["size"] = (ships.maxOf { it.size } + 2).toString()
                    props["maxShipType"] = (ships.maxOf { it.size }).toString()
                }

            logging {
                ln("\"${appPropsFileName}.properties\"")
                props.entries.sortedBy { it.key as String }.logEach { "${it.key} = ${it.value}" }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun JsonValue.asCoord() = asArray().run { (get(0).asInt() to get(1).asInt()) }

    private fun JsonValue.asCollection(): MutableCollection<JsonValue> = asArray().toMutableList()
    val player: Int? get() = props["player"].asInt

    val players: Int? get() = props["players"].asInt
    val size: Int get() = props["size"].asInt!!

    val maxShipType: Int get() = props["maxShipType"].asInt!!
    val playersNumber: Int get() = props["playersNumber"].asInt!!
    val port: Int get() = props[portProperty].asInt!!
    val isToNotifyAll: Boolean get() = props[isToNotifyAllProperty].asBool!!
    val ip: String? get() = props["ip"] as String?

    val ships: Ships? get() = (props["ships"] as Ships?)?.copy()
    val currentPlayer: String get() = props[currentPlayerProperty] as String
    var isServer: Boolean = false
    var isDebug: Boolean = ip != null
    fun isServer(onTrue: () -> Unit) = isServer.so(onTrue)

    val viewSwitchType: ViewSwitch.Type = enumByProperty("viewSwitch")
    val shipsPane: ShipsPane.Type = enumByProperty("shipsPane")

    private inline fun <reified E: Enum<E>> enumByProperty(s: String) =
        E::class.java
            .enumConstants
            .first {
                StringUtils.equalsIgnoreCase(it.name, props[s].asString?.trim())
            }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(property: String) = props[property] as T

    companion object {

        //properties names
        private const val gameTypeProperty = "gameType"
        private const val portProperty = "port"
        private const val currentPlayerProperty = "currentPlayer"
        private const val isToNotifyAllProperty = "isToNotifyAll"

        //ui
        const val battleStartButtonTextForServer = "В бой"
        const val battleStartButtonTextForClient = "Готов"
        const val yourTurnMessage = "Ваш ход"
        const val connectionButtonText = "Подключиться"
        const val squareSize = "квадрат"
        const val widthFieldLabel = "ширина"
        const val heightFieldLabel = "высота"
        const val playersNumberLabel = "игроки"
        const val battleFieldCreationMenuTitle = "Создание поля боя"
        const val leaveBattleFieldText = "Покинуть поле боя"
        const val leaveBattleText = "Покинуть бой"
        const val exitText = "Выход"
        const val ipAddressFieldLabel = "ip"
        const val backButtonText = "Назад"
        const val createNewGameButtonText = "Создать"
        const val joinButtonText = "Присоединиться"

        //fleet grid values
        const val incorrectCellRemovingTime = 50L

        //transitions
        const val buttonHoverTransitionTime = 50L
        const val transitionSteps = 50
        const val fillingTransitionTime = 100L

        val enemySelectionFadeTime: Long
        val startButtonTransitionTime: Long
        val leaveBattleFieldButtonTransitionTime: Long
        val enemyFleetFillTransitionTime: Long


        init {
            fillingTransitionTime.also {
                enemySelectionFadeTime = it * 4
                startButtonTransitionTime = it
                leaveBattleFieldButtonTransitionTime = it
                enemyFleetFillTransitionTime = it * 5
            }
        }


        //app view animation
        const val appViewAnimationGridColumns = 20
        const val appViewAnimationGridRows = 10
        const val appViewAnimationCellSize = 40.0
        const val appViewAnimationTime = 30000.0
    }
}

