package org.home.style

import javafx.css.Styleable
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import org.home.mvc.view.component.button.BattleButton
import tornadofx.InlineCss
import tornadofx.style

object TransitionDSL {
    /**
     * Не удалять неиспользуемый @receiver [InlineCss], он необходим для того, чтобы вызывать [hovering] только внутри
     * функции [Styleable.style] в файлe CSS.kt.
     */
    inline fun InlineCss.hovering(battleButton: BattleButton, createHover: HoverTransition.() -> Unit) {
        battleButton.hoverTransition =
            HoverTransition(battleButton).apply {
                createHover()
                enable()
            }
    }


    inline fun InlineCss.filling(battleButton: Region, createFill: FillTransition.() -> Unit): FillTransition {
        return FillTransition(battleButton)
            .apply {
                createFill()
                enable()
            }
    }

    inline fun Transition.transition(
        from: Color,
        to: Color,
        noinline prop: InlineCss.(Color) -> Unit
    ) {
        add(from to to, prop)
    }
}

