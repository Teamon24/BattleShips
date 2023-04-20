package org.home.utils.functions

fun <T> T?.isNotUnit(function: (T) -> Unit) = ifNot<T>(this !is Unit, function)
fun <T> T?.isNotNull(function: (T) -> Unit) = ifNot<T>(this != null, function)
fun <T> T?.ifNotNull(function: (T) -> Unit) = ifNot<T>(this != null, function)

private fun <T> T?.ifNot(condition: Boolean, function: (T) -> Unit): T? {
    if (condition) {
        function(this!!)
    }
    return this
}