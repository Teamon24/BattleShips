package org.home.utils.extensions

object BooleansExtensions {

    inline infix fun <T> Boolean.then(body: () -> T): T? {
        if (this) {
            return body()
        }
        return null
    }

    inline infix fun <T> T?.or(body: () -> T): T {
        return this ?: body()
    }

    inline infix fun <T> Boolean.then(body: T): T? {
        if (this) {
            return body
        }
        return null
    }

    inline infix fun <T> T?.or(body: T): T {
        return this ?: body
    }

    inline infix fun Boolean.so(body: () -> Unit) {
        if (this) body()
    }

    inline infix fun Boolean.yes(body: () -> Unit): Boolean {
        if (this) body()
        return this
    }

    inline infix fun Boolean.no(body: () -> Unit): Boolean {
        if (!this) body()
        return this
    }
}
