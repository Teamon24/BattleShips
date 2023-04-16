package org.home.app

import org.home.style.AppStyles
import org.home.mvc.view.AppView
import org.koin.core.component.KoinComponent
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.View
import kotlin.reflect.KClass


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

class MainApp: DiApp<AppView>(AppView::class)

