package org.home.app.run

import org.home.app.DebugApp
import org.home.app.MainApp
import org.home.app.di.gameScoped
import org.home.app.di.netControllers
import org.home.app.run.checks.AnimationCheck
import org.home.app.run.checks.MultiscreenCheck
import org.home.utils.DSLContainer
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import tornadofx.App
import tornadofx.launch

fun main(vararg args: String) {
    when(args[0]) {
        "app"               -> run<MainApp>            { +netControllers(args[1]); +gameScoped() }
        "app:battle-on-run" -> run<DebugApp>           { +netControllers(args[1]); +gameScoped() }
        "animation-check"   -> run<AnimationCheck>()
        "multiscreen-check" -> run<MultiscreenCheck>()
    }
}

inline fun <reified T: App> run(addModules: DSLContainer<Module>.() -> Unit = {}) {
    val container = DSLContainer<Module>().apply(addModules)
    GlobalContext.startKoin {
        if (container.elements.isNotEmpty()) {
            modules(*container.elements.toTypedArray())
        }
    }
    launch<T>()
}
