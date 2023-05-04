package org.home.utils.extensions

object BooleansExtensions {

    inline infix fun <T> Boolean.then(body: () -> T) = if (this) body() else null
    inline infix fun <T> T?.or(body: () -> T)= this ?: body()

    inline infix fun <T> Boolean.then(body: T) = if (this) body else null
    inline infix fun <T> T?.or(body: T) = this ?: body

    inline infix fun Boolean.so(body: () -> Unit) = if (this) body() else Unit

    inline infix fun Boolean.yes(body: () -> Unit): Boolean {
        if (this) body()
        return this
    }

    inline infix fun Boolean.no(body: () -> Unit): Boolean {
        if (!this) body()
        return this
    }
}
