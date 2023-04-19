package org.home

import org.home.mvc.view.AppView
import org.home.style.AppStyles
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.View
import tornadofx.launch
import kotlin.reflect.KClass

class MainApp: DiApp<AppView>(AppView::class)

abstract class DiApp<T: View>(view: KClass<T>): App(view, AppStyles::class) {

    init {
        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                val get = getKoin().get<T>(clazz = type)
                return get
            }
        }
    }
}

fun main(args: Array<String>) {
    startKoin(*args)
    launch<MainApp>()
}

fun startKoin(vararg args: String) {
    startKoin {
//        logger(PrintLogger(Level.DEBUG))
        val properties = args[0]
        val player = args[1].toInt()
        val players = args[2].toInt()

        modules(diModule(properties, player, players))
    }
}

