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
        importStylesheet("/${AppStyles.playersListView}.css")
        Thread.currentThread().name = "UI"

        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>) = getKoin().get<T>(clazz = type)
        }
    }

    override fun start(stage: Stage) {
//        stage.initStyle(StageStyle.UNDECORATED)
        stage.width = StageUtils.screenSize().width.toDouble()/2
        stage.height = StageUtils.screenSize().height.toDouble()/2
        stage.isMaximized = false
        super.start(stage)
    }
}