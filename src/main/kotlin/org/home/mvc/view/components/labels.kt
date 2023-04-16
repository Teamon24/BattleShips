package org.home.mvc.view.components

import javafx.scene.control.Label

fun Label.setSize(value: Double) {
    setMinSize(value, value)
    setMaxSize(value, value)
}