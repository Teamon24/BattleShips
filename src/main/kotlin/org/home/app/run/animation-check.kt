package org.home.app.run

import org.home.app.AbstractApp
import org.home.mvc.view.Animations.appViewAnimationGrid
import org.home.app.di.netControllers
import org.koin.core.context.GlobalContext
import tornadofx.View
import tornadofx.launch


fun main() {
    GlobalContext.startKoin {
        modules(netControllers("application-dev"))
    }
    launch<AnimationCheck>()
}

class AnimationCheck: AbstractApp<My2View>(My2View::class)

class My2View: View() {
    override val root = appViewAnimationGrid(50, 50)
}
