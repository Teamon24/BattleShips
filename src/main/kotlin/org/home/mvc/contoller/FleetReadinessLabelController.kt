package org.home.mvc.contoller

import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.paint.Color
import javafx.scene.paint.Color.WHITE
import org.home.app.di.gameScope
import org.home.mvc.GameComponent
import org.home.mvc.view.fleet.ShipTypeLabel
import org.home.mvc.view.fleet.style.FleetGridStyleComponent
import org.home.mvc.view.fleet.style.FleetGridStyleComponent.FleetGreedStyleUpdate.CSS
import org.home.style.AppStyles.Companion.selectedColor
import org.home.style.ColorCounter
import org.home.utils.ColorStepper
import tornadofx.ChangeListener
import tornadofx.style

class FleetReadinessLabelController: GameComponent() {
    internal val fleetReadinessPaneStyleComponent by gameScope<FleetGridStyleComponent>(CSS)

    private val initial: Color = WHITE
    private var currentColor = initial
    private val colorTransition = initial to selectedColor
    private lateinit var inc: ColorCounter
    private lateinit var dec: ColorCounter
    private var last: Int = 0
    private lateinit var shipsNumberProp: SimpleIntegerProperty
    private lateinit var view: ShipTypeLabel

    fun create(type: Int, shipsNumberProp: SimpleIntegerProperty): ShipTypeLabel {
        initColorStepper(shipsNumberProp, type)
        create(type)
        setInitialColor()

        shipsNumberProp.addListener(ChangeListener { _, oldValue, newValue ->
            val incremented = oldValue.toInt() > newValue.toInt()
            currentColor = incremented.then { inc(currentColor) }.or { dec(currentColor) }
            view.style {
                backgroundColor += currentColor
            }
        })

        return view
    }

    private fun initColorStepper(shipsNumberProp: SimpleIntegerProperty, type: Int) {
        this.shipsNumberProp = shipsNumberProp
        last = modelView.getShipsNumber(type)
        ColorStepper(last)
            .addStep(colorTransition)
            .run {
                inc = getColorInc(colorTransition)
                dec = getColorDecr(colorTransition)
            }
    }

    private fun create(type: Int) {
        view = ShipTypeLabel(type)
    }

    private fun setInitialColor() {
        val current = shipsNumberProp.value.toInt()
        repeat(last - current) { currentColor = inc(currentColor) }
        view.style { backgroundColor += currentColor}
    }
}
