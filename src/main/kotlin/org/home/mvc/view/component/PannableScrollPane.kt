package org.home.mvc.view.component

import javafx.event.EventTarget
import javafx.scene.control.ScrollPane
import tornadofx.opcr

class PannableScrollPane : ScrollPane() {

    init {
        isPannable = true
        scrollBarPolicy()
    }

    private fun scrollBarPolicy() {
        hbarPolicy = ScrollBarPolicy.NEVER
        vbarPolicy = ScrollBarPolicy.NEVER
    }

    companion object {
        fun EventTarget.pannableScrollPane(op: PannableScrollPane.() -> Unit) = opcr(this, PannableScrollPane(), op)
    }
}