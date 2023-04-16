package org.home.utils

fun threadPrintln(message: String) {
    println("[${Thread.currentThread().name}]: $message")
}