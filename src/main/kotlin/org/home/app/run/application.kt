package org.home.app.run

import home.extensions.AnysExtensions.invoke
import home.extensions.CollectionsExtensions.isNotEmpty
import org.home.app.BattleOnRunApp
import org.home.app.MainApp
import org.home.app.di.gameModule
import org.home.app.run.checks.AnimationCheck
import org.home.app.run.checks.MultiscreenCheck
import org.home.utils.DSLContainer
import org.home.utils.dslElements
import org.home.utils.logTitle
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.Module
import tornadofx.App
import tornadofx.launch

fun main(vararg args: String) {
    args {
        logTitle(get(0).uppercase(), titleSymbolsNumber = 40)
        when(get(0)) {
            "app"               -> run<MainApp>            { +gameModule(get(1)); }
            "app:battle-on-run" -> run<BattleOnRunApp>     { +gameModule(get(1)); }
            "check:animation"   -> run<AnimationCheck>()
            "check:multiscreen" -> run<MultiscreenCheck>()
        }
    }
}

inline fun <reified T: App> run(crossinline addModules: DSLContainer<Module>.() -> Unit = {}) {
    startKoin {
        dslElements(addModules).let { modules ->
            modules.isNotEmpty {
                modules(modules)
            }
        }
    }
    launch<T>()
}
