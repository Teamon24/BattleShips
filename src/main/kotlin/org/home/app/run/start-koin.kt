package org.home.app.run

import org.home.app.di.gameScoped
import org.home.app.di.netControllers
import org.koin.core.context.GlobalContext.startKoin

fun startKoin(vararg args: String) {
    startKoin {
        modules(netControllers(properties = args[0]), gameScoped())
    }
}