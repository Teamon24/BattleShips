package org.home.mvc

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import org.home.mvc.model.Ships
import org.home.mvc.model.ship
import org.home.utils.extensions.StringBuildersExtensions.ln
import org.home.utils.logEach
import org.home.utils.logging
import java.util.*

class ApplicationProperties(private val appPropsFileName: String = "application") {

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
                ?.let { props["ships"] = it }

            props[portProperty] = props[portProperty].asInt!!
            props[isToNotifyAllProperty] = props[isToNotifyAllProperty].asBool!!

            logging {
                ln("\"${appPropsFileName}.properties\"")
                props.entries.logEach { "${it.key} = ${it.value}" }
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
    val ships: Ships? get() = props["ships"] as Ships?

    val ip: String get() = props["ip"] as String
    val port: Int get() = props["port"] as Int

    val currentPlayer: String get() = props[currentPlayerProperty] as String
    val isToNotifyAll: Boolean get() = props[isToNotifyAllProperty] as Boolean

    var isServer: Boolean = false
        set(value) {
            props[isServerProperty] = value;
            field = value
        }
        get() { return props[isServerProperty] as Boolean }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(property: String) = props[property] as T

    companion object {
        //properties names
        private const val isServerProperty = "isServer"
        private const val gameTypeProperty = "gameType"
        private const val portProperty = "port"
        private const val currentPlayerProperty = "currentPlayer"
        private const val isToNotifyAllProperty = "isToNotifyAll"

        //ui
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
        const val incorrectCellRemovingTime = 100L

        //transitions
        const val transitionSteps = 50
        const val fillingTransitionTime = 150L
        const val startButtonTransitionTime = 150L
        const val leaveBattleFieldButtonTransitionTime = 150L
        const val defeatFillTransitionTime = 150L
        const val buttonHoverTransitionTime = 50L

        //app view animation
        const val appViewAnimationGridSize = 40
        const val appViewAnimationCellSize = 40.0
        const val appViewAnimationTime = 30000.0
    }
}

