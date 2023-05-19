package org.home.app

import javafx.stage.Stage
import org.home.mvc.StageUtils
import org.home.style.AppStyles
import org.koin.core.component.KoinComponent
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.View
import tornadofx.importStylesheet
import kotlin.reflect.KClass

abstract class AbstractApp<T : View>(view: KClass<T>) : App(view, AppStyles::class) {
    init {
        currentThreadName("UI")
        FX.dicontainer = diContainer()
    }

    override fun start(stage: Stage) {
//        stage.initStyle(StageStyle.UNDECORATED)
        stage.width = StageUtils.screenSize().width.toDouble()/2
        stage.height = StageUtils.screenSize().height.toDouble()/2
        stage.isMaximized = false
        super.start(stage)
    }

    private fun currentThreadName(name: String) {
        Thread.currentThread().name = name
    }

    private fun diContainer() = object : DIContainer, KoinComponent {
       override fun <T : Any> getInstance(type: KClass<T>) = getKoin().get<T>(clazz = type)
   }
}