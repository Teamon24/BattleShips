package org.home.utils

import org.home.utils.InfiniteTryBase.Companion.catch
import org.home.utils.InfiniteTryBase.Companion.handle
import org.home.utils.InfiniteTryBase.Companion.start
import org.home.utils.InfiniteTryFor.Companion.infiniteTryFor
import org.home.utils.extensions.AtomicBooleansExtensions.atomic
import org.home.utils.extensions.AtomicBooleansExtensions.toggleAfter
import java.io.EOFException
import java.net.SocketException
import kotlin.reflect.full.createInstance

fun main() {
    val exceptions = mutableListOf(
        SocketException::class,
        EOFException::class,
        InterruptedException::class)

    val exceptions2 = mutableListOf(
        IllegalArgumentException::class,
        ClassNotFoundException::class,
        NoSuchElementException::class)

    val thrown = mutableListOf<Exception>()
    val thrown2 = mutableListOf<Exception>()

    val loopLogic = listOf(1, 2).run {
        infiniteTryFor {
            Thread.sleep(250)
            throw (exceptions + exceptions2).random().createInstance()
        } catch {
            exceptions.forEach { + it }
            handle { ex, element ->
                println(ex.javaClass)
                thrown.add(ex)
            }
            exceptions2.forEach { + it }
            handle { ex, element ->
                println(ex.javaClass)
                thrown2.add(ex)
            }
        }
    }

    loopLogic start true.atomic.toggleAfter(3000)

    val thrownClasses = thrown.map { it::class }.toHashSet()
    assert(thrownClasses.size == exceptions.size)

    val thrownClasses2 = thrown2.map { it::class }.toHashSet()
    assert(thrownClasses2.size == exceptions2.size)

    class MyEx: RuntimeException()

    val notCatchableEx = MyEx::class
    try {
        exceptions.add(notCatchableEx)
        loopLogic start true.atomic
    } catch (e: Throwable) {
        println()
        println(e::class)
        assert(e::class == notCatchableEx)
    }

}

