package org.home.style

import javafx.scene.paint.Color

object ColorUtils {
    fun Color.brighter(i: Int) = apply { repeat(i) { this.brighter() } }

    fun Color.withOpacity(d: Double): Color {
        return Color.color(red, green, blue, d)
    }

    fun Color.toRGBCode(): String {
        return String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
    }
}