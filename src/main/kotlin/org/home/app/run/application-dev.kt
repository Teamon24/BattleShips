package org.home.app.run

import org.home.app.MainApp
import tornadofx.launch

fun main(vararg args: String) {
    startKoin(*args)
    launch<MainApp>()
}