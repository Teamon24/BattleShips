package org.home

import org.home.app.MainApp
import org.home.app.diModule
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import tornadofx.launch

fun main(args: Array<String>) {
    startKoin(args[0])
    launch<MainApp>()
}

fun startKoin(props: String) {
    startKoin {
        logger(PrintLogger(Level.DEBUG))
        modules(diModule(props))
    }
}

