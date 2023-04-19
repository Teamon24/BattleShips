package org.home

import org.home.utils.ln
import org.home.utils.logger
import java.util.*


class ApplicationProperties(
    private val appPropsFileName: String,
    val player: Int? = null,
    val players: Int? = null
) {

    private val props = Properties().apply {
        put(gameTypeProperty, "")
    }

    init {
        try {
            val s = "/${appPropsFileName}.properties"

            val resourceAsStream = Companion::class.java.getResourceAsStream(s)
            resourceAsStream.use { stream -> props.load(stream) }

            props["port"] = (props["port"] as String).toInt()
            logger {
                ln("properties: ${appPropsFileName}.properties")
                ln("properties content")
                ln(props.entries.joinToString("\n") { "${it.key}:${it.value}" })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val ip: String get() { return props["ip"] as String }
    val port: Int get() { return props["port"] as Int }
    val currentPlayer: String get() { return props[currentPlayerProperty] as String }

    var isServer: Boolean = false
        set(value) { props[isServerProperty] = value; field = value }
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
        private const val currentPlayerProperty = "currentPlayer"

        const val connectionButtonText = "Подключиться"
        const val squareSize = "квадрат"
        const val widthFieldLabel = "ширина"
        const val heightFieldLabel = "высота"
        const val playersNumberLabel = "игроки"
        const val battleFieldCreationMenuTitle = "Создание поля боя"
        const val ipAddressFieldLabel = "ip"
        const val creationButtonText = "Создать поле боя"
        const val joinButtonText = "Присоединиться"
        const val delayTime = 300L
    }
}

