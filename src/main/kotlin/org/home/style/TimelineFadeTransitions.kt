package org.home.style

import home.extensions.AnysExtensions.name
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.scene.Node
import org.home.style.TimelineUtils.keyFrame
import org.home.style.TimelineUtils.play
import org.home.utils.log

object TimelineFadeTransitions {
    fun fadeOver(
        fadeTime: Double,
        fadeInNode: Node,
        fadeOutNode: Node = fadeInNode,
        onFadeInFinished: (ActionEvent?) -> Unit
    ) {
        val startFadeIn  = keyFrame(fadeInNode.opaque()       , fadeTime/4 * 0)
        val endFadeIn    = keyFrame(fadeInNode.transparent()  , fadeTime/4 * 1)
        val startFadeOut = keyFrame(fadeOutNode.transparent() , fadeTime/4 * 2    , onFadeInFinished)
        val endFadeOut   = keyFrame(fadeOutNode.opaque()      , fadeTime/4 * 3)
        Timeline().play(startFadeIn, endFadeOut, startFadeOut , endFadeIn)
        log { "$name FADE OVER is done" }
    }

    fun fadeIn(fadeTime: Double, fadeInNode: Node, onFinish: (ActionEvent) -> Unit = {}) {
        val startFadeIn = keyFrame(fadeInNode.opaque()      , fadeTime / 2)
        val endFadeIn   = keyFrame(fadeInNode.transparent() , fadeTime      , onFinish)
        Timeline().play(startFadeIn, endFadeIn)
        log { "$name FADE IN is done" }
    }

    fun fadeOut(fadeTime: Double, fadeOutNode: Node, onStart: (ActionEvent) -> Unit = {}) {
        val makeFade     = keyFrame(fadeOutNode.transparent() , 0.0 , onStart)
        val startFadeOut = keyFrame(fadeOutNode.transparent() , fadeTime/2)
        val endFadeOut   = keyFrame(fadeOutNode.opaque()      , fadeTime)
        Timeline().play(makeFade, startFadeOut, endFadeOut)
        log { "$name FADE OUT is done" }
    }



    private fun Node.transparent() = KeyValue(opacityProperty(), 0.0)
    private fun Node.opaque() = KeyValue(opacityProperty(), 1.0)
}