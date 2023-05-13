package org.home.run

import org.home.app.MainApp
import org.home.app.di.diDev
import org.koin.core.context.GlobalContext.startKoin
import tornadofx.launch

fun main(vararg args: String) {
    startKoin(*args)
    launch<MainApp>()
}

fun startKoin(vararg args: String) {
    startKoin {
        val properties = args[0]
        modules(diDev(properties))
    }
}

