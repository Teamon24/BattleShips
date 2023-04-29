package org.home

import tornadofx.launch

fun main() {
    startKoin("application", "0", "6")
    launch<MainApp>()
}
