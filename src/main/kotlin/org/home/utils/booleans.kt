package org.home.utils

import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 */
fun Boolean.atomic() = AtomicBoolean(this)
operator fun AtomicBoolean.invoke() = this.get()
operator fun AtomicBoolean.invoke(value: Boolean) = this.set(value)