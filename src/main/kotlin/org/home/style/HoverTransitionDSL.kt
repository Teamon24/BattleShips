package org.home.style

import javafx.css.Styleable
import javafx.scene.paint.Color
import org.home.mvc.view.components.BattleButton
import tornadofx.InlineCss

object HoverTransitionDSL {
    /**
     * Не удалять неиспользуемый @receiver [InlineCss], он необходим для того, чтобы вызывать [hover] только внутри
     * функции [Styleable.style] в файлe CSS.kt.
     */
    inline fun InlineCss.hover(battleButton: BattleButton, createHover: HoverTransition.() -> Unit) {
        battleButton.hoverTransition = HoverTransition(battleButton).apply { createHover() }
    }

    inline fun HoverTransition.transition(
        from: Color,
        to: Color,
        noinline prop: InlineCss.(Color) -> Unit
    ) {
        add(from to to, prop)
    }
}

