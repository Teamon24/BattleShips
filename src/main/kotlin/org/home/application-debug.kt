package org.home

import org.home.mvc.view.app.MainApp
import tornadofx.launch

fun main() {
    startKoin("application", "0", "6")
    launch<MainApp>()
}
