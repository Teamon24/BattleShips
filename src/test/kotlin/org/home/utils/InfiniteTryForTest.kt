package org.home.utils

import org.home.utils.InfiniteTryBase.Companion.catch
import org.home.utils.InfiniteTryBase.Companion.doWhile
import org.home.utils.InfiniteTryBase.Companion.handle
import org.home.utils.InfiniteTryFor.Companion.infiniteTryFor
import home.extensions.AnysExtensions.name
import home.extensions.AtomicBooleansExtensions.atomic
import home.extensions.AtomicBooleansExtensions.toggleAfter
import home.extensions.BooleansExtensions.so
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import java.io.EOFException
import java.net.SocketException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Test for [InfiniteTryFor]
 */
class InfiniteTryForTest {
    private val countingThreadSleep = 1000L

    private lateinit var exceptions: MutableList<KClass<out Exception>>
    private lateinit var exceptions2: MutableList<KClass<out Exception>>
    private lateinit var thrown: MutableList<Exception>
    private lateinit var thrown2: MutableList<Exception>

    @BeforeEach
    fun beforeEach() {
        exceptions = mutableListOf(
            SocketException::class,
            EOFException::class,
            InterruptedException::class)

        exceptions2 = mutableListOf(
            IllegalArgumentException::class,
            ClassNotFoundException::class,
            NoSuchElementException::class)

        thrown = mutableListOf()
        thrown2 = mutableListOf()
    }

    @Test
    fun test() {
        loop().doWhile(true.atomic.toggleAfter(countingThreadSleep))

        val thrownClasses = thrown.map { it::class }.toHashSet()
        val thrownClasses2 = thrown2.map { it::class }.toHashSet()

        Assertions.assertTrue(thrownClasses.size == exceptions.size)
        Assertions.assertTrue(thrownClasses.containsAll(exceptions))
        Assertions.assertTrue(thrownClasses2.size == exceptions2.size)
        Assertions.assertTrue(thrownClasses2.containsAll(exceptions2))
    }


    @Test
    fun test2() {
        class MyEx : RuntimeException()

        try {
            loop(MyEx()).doWhile(true.atomic)
        } catch (e: Throwable) {
            Assertions.assertTrue(e::class == MyEx::class)
            return
        }

        throw AssertionFailedError("${MyEx::class.name} was not thrown")

    }

    private fun loop(vararg notCatchableEx: Exception): InfiniteTryBase<Int, (Exception, Int) -> Unit> {
        val exIterator = createIterator(exceptions + exceptions2)
        return listOf(1).infiniteTryFor {
            exIterator.hasNext().so { throw exIterator.next().createInstance() }
            notCatchableEx.isNotEmpty().so { throw notCatchableEx.first() }
        } catch {
            +exceptions
            handle { ex, _ -> handleBody(ex, thrown, "ex1") }

            +exceptions2
            handle { ex, _ -> handleBody(ex, thrown2, "ex2") }
        }
    }

    private fun createIterator(allExceptions: List<KClass<out Exception>>): Iterator<KClass<out Exception>> {
        return object : Iterator<KClass<out Exception>>{
            private val ex = allExceptions
            private var i: Int = 0
            override fun next(): KClass<out Exception> {
                if(i >= ex.size) throw NoSuchElementException()
                return ex[i++]
            }
            override fun hasNext(): Boolean {
                if(i >= ex.size) return false
                return true
            }
        }
    }

    private fun handleBody(ex: Exception, thrown2: MutableList<Exception>, s: String) {
        println("$s = ${ex.javaClass}")
        thrown2.add(ex)
    }
}
