package org.home.mvc.contoller

import javafx.scene.layout.GridPane
import org.home.style.AppStyles
import tornadofx.addClass

class ShipsTypesPane(val transposed: Boolean = false): GridPane() {
    init {
        addClass(AppStyles.gridMargin)
    }
}