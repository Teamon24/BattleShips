package org.home.mvc

import org.home.utils.extensions.ln
import org.home.utils.log
import org.home.utils.logEach
import org.home.utils.logging
import java.util.*


class ApplicationProperties(
    private val appPropsFileName: String = "application",
    val player: Int? = null,
    val players: Int? = null
) {

    private val props = Properties().apply {
        put(gameTypeProperty, "")
    }

    init {
        try {
            val propertiesName = "/${appPropsFileName}.properties"
             Companion::class.java
                    .getResourceAsStream(propertiesName)
                    .use { stream -> props.load(stream) }

            props[portProperty] = (props[portProperty] as String).toInt()
            props[isToNotifyAllProperty] = (props[isToNotifyAllProperty] as String).toBoolean()

            logging {
                ln("\"${appPropsFileName}.properties\"")
                props.entries.logEach { "${it.key} = ${it.value}" }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val ip: String get() = props["ip"] as String
    val port: Int get() = props["port"] as Int

    val currentPlayer: String get() = props[currentPlayerProperty] as String
    val isToNotifyAll: Boolean get() = props[isToNotifyAllProperty] as Boolean

    var isServer: Boolean = false
        set(value) {
            props[isServerProperty] = value; field = value
            log { "$isServerProperty = $value" }

        }
        get() { return props[isServerProperty] as Boolean }

    private operator fun get(propName: String) =
        props.getProperty(propName) ?: throw RuntimeException("Property '$propName' is absent")

    operator fun <T> set(property: String, value: T) {
        props[property] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(property: String) = props[property] as T

    companion object {
        private const val isServerProperty = "isServer"
        private const val gameTypeProperty = "gameType"
        private const val portProperty = "port"
        private const val currentPlayerProperty = "currentPlayer"
        private const val isToNotifyAllProperty = "isToNotifyAll"

        const val connectionButtonText = "Подключиться"
        const val squareSize = "квадрат"
        const val widthFieldLabel = "ширина"
        const val heightFieldLabel = "высота"
        const val playersNumberLabel = "игроки"
        const val battleFieldCreationMenuTitle = "Создание поля боя"
        const val ipAddressFieldLabel = "ip"
        const val creationButtonText = "Создать поле боя"
        const val joinButtonText = "Присоединиться"
        const val delayTime = 100L
    }
}

