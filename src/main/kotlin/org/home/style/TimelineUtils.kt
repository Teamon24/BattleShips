package org.home.style

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.util.Duration.millis

object TimelineUtils {
    fun keyFrame(keyValue: KeyValue, d: Double, onFinish: (ActionEvent) -> Unit = {}) =
        KeyFrame(millis(d), onFinish, keyValue)

    fun Timeline.play(vararg frames: KeyFrame) {
        keyFrames.addAll(frames)
        cycleCount = 1
        play()
    }

    fun Timeline.play(frames: Collection<KeyFrame>) {
        keyFrames.addAll(frames)
        cycleCount = 1
        play()
    }
}
