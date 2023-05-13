package org.home.run

import org.home.app.MainApp
import tornadofx.launch

fun main() {
    startKoin("application-dev")
    launch<MainApp>()
}
