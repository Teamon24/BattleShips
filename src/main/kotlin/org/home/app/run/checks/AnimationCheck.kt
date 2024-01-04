package org.home.app.run.checks

import org.home.app.AbstractApp
import org.home.mvc.Animations.appViewAnimationGrid
import tornadofx.View

class AnimationCheck: AbstractApp<AnimationCheckView>(AnimationCheckView::class)

class AnimationCheckView: View() {
    override val root = appViewAnimationGrid(20, 10, 40.0, 30000.0)
}
