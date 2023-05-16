package org.home.app.run

import org.home.app.MainApp
import tornadofx.launch

fun main() {
    startKoin("application-dev")
    launch<MainApp>()
}
