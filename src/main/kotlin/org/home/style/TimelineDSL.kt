package org.home.style

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.control.Labeled
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import org.home.app.ApplicationProperties.Companion.enemyFleetFillTransitionTime
import org.home.style.FillTransitions.backgroundFill
import org.home.style.FillTransitions.textFill
import org.home.style.TimelineUtils.play
import org.home.utils.ColorStepper
import org.home.utils.StyleUtils.backgroundColor
import tornadofx.millis
import tornadofx.runLater

object TimelineDSL {

    class KeyValues(
        val values: MutableList<MutableList<KeyValue>> = mutableListOf()
    ) {
        val t = 10;
        fun Region.background(color: Color) {
            val colorInc = ColorStepper(t)
                .set(backgroundColor to color)
                .getColorInc(backgroundColor to color)

            val temp = mutableListOf<KeyValue>()
            var tempColor = backgroundColor
            for (i in 1..t) {
                temp.add(backgroundFill(tempColor))
                tempColor = colorInc(tempColor)
            }

            values.add(temp)
        }

        fun Labeled.text(color: Color) {
            val colorInc = ColorStepper(t)
                .set(backgroundColor to color)
                .getColorInc(backgroundColor to color)

            val temp = mutableListOf<KeyValue>()
            var tempColor = backgroundColor
            for (i in 1..t) {
                temp.add(textFill(tempColor))
                tempColor = colorInc(tempColor)
            }

            values.add(temp)
        }
    }

    fun keyValues(addKeyValues: KeyValues.() -> Unit) {
        val keyValuesList = KeyValues()
        keyValuesList.addKeyValues()
        val map = hashMapOf<Int, MutableList<KeyValue>>()
        val size = keyValuesList.values.first().size
        for (i in 0 until size) {
            map[i] = mutableListOf()
        }

        keyValuesList.values.map { keyValues ->
            for (i in 0 until size) {
                map[i]!!.add(keyValues[i])
            }
        }

        val timeStep = enemyFleetFillTransitionTime / size
        val frames = map.entries.map { (i, keyValues) ->
            KeyFrame((i * timeStep).millis, *keyValues.toTypedArray())
        }
        Timeline().apply {
            runLater {
                play(frames)
            }
        }
    }
//    frames.map { it.time to it.values.filter { it.endValue is Background }.map { ((it.endValue as Background).fills[0].fill as Color) } }
}