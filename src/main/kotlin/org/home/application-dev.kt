package org.home

import org.home.mvc.view.app.MainApp
import org.koin.core.context.GlobalContext.startKoin
import tornadofx.launch

fun main(vararg args: String) {
    startKoin(*args)
    launch<MainApp>()
}

fun startKoin(vararg args: String) {
    startKoin {
        val properties = args[0]
        val player = args[1].toInt()
        val players = args[2].toInt()
        modules(diDev(properties, player, players))
    }
}

