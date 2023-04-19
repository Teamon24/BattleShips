package org.home.utils

fun threadPrintln(message: String) {
    println(threadLog(message))
}

fun threadPrintln(any: Any) {
    println(threadLog(any))
}

fun threadPrint(any: Any) {
    print(threadLog(any))
}

fun threadLog(any: Any) = "[${Thread.currentThread().name}]: $any"