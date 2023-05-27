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
import org.home.utils.ColorStepper
import org.home.utils.StyleUtils.backgroundColor
import tornadofx.millis

object TimelineDSL {

    private const val defaulSteps = 10

    class KeyValues(val steps: Int = defaulSteps, val values: MutableList<MutableList<KeyValue>> = mutableListOf()) {
        fun Region.background(color: Color) {
            val initialColor = backgroundColor
            val colorTransition = initialColor to color

            val colorInc = ColorStepper(steps).addStep(colorTransition).getColorInc(colorTransition)

            val temp = mutableListOf<KeyValue>()
            var tempColor = initialColor
            for (i in 1..steps) {
                temp.add(backgroundFill(tempColor))
                tempColor = colorInc(tempColor)
            }

            values.add(temp)
        }

        fun Labeled.text(color: Color) {
            val initial = backgroundColor
            val colorTransition = initial to color

            val colorInc = ColorStepper(steps).addStep(colorTransition).getColorInc(colorTransition)

            val temp = mutableListOf<KeyValue>()
            var tempColor = initial
            for (i in 1..steps) {
                temp.add(textFill(tempColor))
                tempColor = colorInc(tempColor)
            }

            values.add(temp)
        }
    }

    fun play(addKeyValues: KeyValues.() -> Unit) {
        timeline(addKeyValues = addKeyValues).apply { play() }
    }

    private fun timeline(steps: Int = defaulSteps, addKeyValues: KeyValues.() -> Unit): Timeline {
        val keyValuesList = KeyValues(steps)
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
        return Timeline().apply {
            keyFrames.addAll(frames)
        }
    }
}