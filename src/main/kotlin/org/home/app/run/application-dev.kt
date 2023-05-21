package org.home.app.run

import org.home.app.MainApp
import org.home.app.di.gameScoped
import org.home.app.di.netControllers
import org.koin.core.context.GlobalContext.startKoin
import tornadofx.launch

fun main(vararg args: String) {
    startKoin(*args)
    launch<MainApp>()
}

fun startKoin(vararg args: String) {
    startKoin {
        modules(netControllers(properties = args[0]), gameScoped())
    }
}

