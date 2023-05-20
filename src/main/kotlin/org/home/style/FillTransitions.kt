package org.home.style

import javafx.animation.Interpolator
import javafx.animation.KeyValue
import javafx.scene.control.Labeled
import javafx.scene.layout.Background
import javafx.scene.layout.Region
import javafx.scene.paint.Color

object FillTransitions {
    fun Region.backgroundFill(color: Color) = KeyValue(
        backgroundProperty(),
        Background.fill(color),
        Interpolator.EASE_OUT
    )

    fun Labeled.textFill(color: Color) = KeyValue(
        textFillProperty(),
        color,
        Interpolator.EASE_OUT
    )
}