package org.home.app

import tornadofx.launch

fun main() {
    startKoin("application", "0", "3")
    launch<MainApp>()
}
