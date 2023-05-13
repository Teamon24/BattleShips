package org.home.run

import javafx.geometry.Rectangle2D
import javafx.scene.Parent
import javafx.scene.layout.GridPane
import javafx.stage.Screen
import tornadofx.App
import tornadofx.View
import tornadofx.launch
import java.awt.HeadlessException
import java.awt.MouseInfo



fun main() {
    Thread.sleep(2000)
    launch<MultiscreenCheck>()
}

class MultiscreenCheck: App(MyView::class)

class MyView: View() {
    private val size = 1000.0

    init {
        val startUpLocation = StartUpLocation(size, size)
        primaryStage.x = startUpLocation.xPos
        primaryStage.y = startUpLocation.yPos
    }

    override val root: Parent
        get() = GridPane().apply {
            minWidth = size
            minHeight = size
            prefWidth = size
            prefHeight = size
            maxWidth = size
            maxHeight = size
        }
}

/**
 * X-Y position of a Window on active screen at startup if more than one screen.
 * Note: This works smooth only if the outer most AnchorPane size is fixed at
 * design time. This is because, if the size is not fixed JavaFX calculates
 * Window size after Stage.show() method. If the pref size is fixed, then use
 * this class in WindowEvent.WINDOW_SHOWING event, or if the pref size is set to
 * USE_COMPUTED_SIZE then use it in WindowEvent.WINDOW_SHOWN event (this will
 * give a quick splash Window though). Feel free to improve and share this code.
 * I am new to JavaFX so tired what I know so far. Tested on Windows but need
 * more attention to Linux and Mac
 *
 * @author
 */
class StartUpLocation(windowWidth: Double, windowHeight: Double) {
    /**
     * @return the top left X Position of Window on Active Screen */
    var xPos = 0.0

    /**
     * @return the top left Y Position of Window on Active Screen */
    var yPos = 0.0

    /**
     * Get Top Left X and Y Positions for a Window to centre it on the
     * currently active screen at application startup
     */
    init {
        // Get X Y of start-up location on Active Screen
        // simple_JavaFX_App
        try {
            // Get current mouse location, could return null if mouse is moving Super-Man fast
            val p = MouseInfo.getPointerInfo().location
            // Get list of available screens
            val screens: List<Screen>? = Screen.getScreens()
            if (p != null && screens != null && screens.size > 1) {
                // Screen bounds as rectangle
                var screenBounds: Rectangle2D
                // Go through each screen to see if the mouse is currently on that screen
                for (screen in screens) {
                    screenBounds = screen.visualBounds
                    // Trying to compute Left Top X-Y position for the Application Window
                    // If the Point p is in the Bounds
                    if (screenBounds.contains(p.x.toDouble(), p.y.toDouble())) {
                        // Fixed Size Window Width and Height
                        xPos = screenBounds.minX + (screenBounds.maxX - screenBounds.minX - windowWidth) / 2
                        yPos = screenBounds.minY + (screenBounds.maxY - screenBounds.minY - windowHeight) / 2
                    }
                }
            }
        } catch (headlessException: HeadlessException) {
            headlessException.printStackTrace()
        }
    }
}
