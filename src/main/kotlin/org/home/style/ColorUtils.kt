package org.home.style

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import home.to

object ColorUtils {
    val String.color get() = Paint.valueOf(this) as Color
    val Paint.color get() = this as Color

    fun Color.opacity(d: Double): Color {
        return Color.color(red, green, blue, d)
    }

    fun red(value: Double): Color {
        return Color.color(value, 0.0, 0.0)
    }

    fun Color.toRGBCode(): String {
        return String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
    }

    operator fun Color.invoke() = red to green to blue
}