package org.home.run

import org.home.app.AbstractApp
import org.home.mvc.view.Animations.appViewAnimationGrid
import org.home.app.di.singletons
import org.koin.core.context.GlobalContext
import tornadofx.View
import tornadofx.launch


fun main() {
    GlobalContext.startKoin {
        modules(singletons()("application-dev"))
    }
    launch<AnimationCheck>()
}

class AnimationCheck: AbstractApp<My2View>(My2View::class)

class My2View: View() {
    override val root = appViewAnimationGrid(50, 50)
}
