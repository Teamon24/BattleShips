package org.home.app.run

import org.home.app.DebugApp
import tornadofx.launch

fun main(vararg args: String) {
    startKoin(*args)
    launch<DebugApp>()
}
