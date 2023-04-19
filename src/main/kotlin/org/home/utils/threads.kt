package org.home.utils

fun threadPrintln(message: String) {
    println("[${Thread.currentThread().name}]: $message")
}

fun threadPrintln(any: Any) {
    println("[${Thread.currentThread().name}]: $any")
}

fun threadPrint(any: Any) {
    print("[${Thread.currentThread().name}]: $any")
}