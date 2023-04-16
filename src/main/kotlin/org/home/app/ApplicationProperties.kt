package org.home.app

import java.util.*


class ApplicationProperties(appPropsFileName: String) {

    private val properties = Properties().apply {
        put(gameType, "")
        put(isServer, false)
    }

    operator fun <T> set(property: String, value: T) {
        properties[property] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(property: String) = properties[property] as T

    init {
        try {
            Companion::class.java.getResourceAsStream("/${appPropsFileName}.properties").use { stream ->
                properties.load(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val currentPlayer = properties.getProperty("currentPlayer") ?: throw RuntimeException("")

    companion object {
        const val isServer = "isServer"
        const val gameType = "gameType"
        const val squareSize = "квадрат"
        const val widthFieldLabel = "ширина"
        const val heightFieldLabel = "высота"
        const val playersNumberLabel = "игроки"
        const val battleFieldCreationMenuTitle = "Создание поля боя"
        const val ipFieldLabel = "ip"
        const val creationButtonText = "Создать поле боя"
        const val joinButtonText = "Присоединиться"
        const val delayTime = 300L
    }
}

