package org.home.app

import javafx.stage.Stage
import org.home.app.di.Scopes
import org.home.style.AppStyles
import org.koin.core.component.KoinComponent
import tornadofx.App
import tornadofx.Component
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.ScopedInstance
import tornadofx.View
import tornadofx.find
import tornadofx.importStylesheet
import kotlin.properties.ReadOnlyProperty
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
        stage.isMaximized = false
        super.start(stage)
    }

    companion object {
        inline fun <reified T> newGame(): ReadOnlyProperty<Component, T> where
                T : Component,
                T : ScopedInstance = ReadOnlyProperty { _, _ -> find(Scopes.gameScope) }
    }
}