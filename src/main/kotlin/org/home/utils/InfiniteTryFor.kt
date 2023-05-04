package org.home.utils

import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

sealed class InfiniteTryBase<E, H> {
    private val handlers = mutableMapOf<H, List<KClass<out Exception>>>()
    private val exceptions = mutableListOf<KClass<out Exception>>()
    abstract val emptyHandler: H

    operator fun KClass<out Exception>.unaryPlus(): KClass<out Exception> {
        exceptions.add(this)
        return this
    }

    operator fun Collection<KClass<out Exception>>.unaryPlus() {
        forEach { +it }
    }

    fun putHandler(b: H) {
        handlers[b] = exceptions.toMutableList()
        exceptions.clear()
    }

    fun getHandler(ex: Exception): H {
        val handlerAndExceptions = handlers.entries.firstOrNull { handlerAndExs ->
            ex::class in handlerAndExs.value
        }

        handlerAndExceptions ?: throw ex

        return handlerAndExceptions.key
    }

    companion object {
        infix fun <E, H> InfiniteTryBase<E, H>.doWhile(condition: AtomicBoolean) {
            while (condition()) { loopBody() }
            log { "i'm done" }
        }

        infix fun <E, H> InfiniteTryBase<E, H>.handle(handler: H) {
            putHandler(handler)
        }

        inline fun <E, H> InfiniteTryBase<E, H>.noHandle() {
            putHandler(emptyHandler)
        }

        inline infix fun <E, H> InfiniteTryBase<E, H>.catch(
            function: InfiniteTryBase<E, H>.() -> Unit
        ): InfiniteTryBase<E, H> {
            function()
            return this
        }
    }

    abstract fun loopBody()
}

class InfiniteTryFor<E>(
    val elements: Collection<E>,
    val body: (E) -> Unit): InfiniteTryBase<E, (Exception, E) -> Unit>() {
    override val emptyHandler = { _: Exception, _: E -> }

    override fun loopBody() {
        elements.forEach { element ->
            try { body(element) }
            catch (ex: Exception) {
                getHandler(ex)(ex, element)
            }
        }
    }

    companion object {
        infix fun <E> Collection<E>.infiniteTryFor(forEach: (E) -> Unit): InfiniteTryFor<E> {
            return InfiniteTryFor(this, forEach)
        }
    }
}


class InfiniteTry(val body: () -> Unit): InfiniteTryBase<Unit, (Exception) -> Unit>() {
    override val emptyHandler = { _: Exception -> }

    override fun loopBody() {
        try {
            body()
        } catch (ex: Exception) {
            val handler = getHandler(ex)
            handler(ex)
        }
    }

    companion object {
        fun infiniteTry(body: () -> Unit): InfiniteTry {
            return InfiniteTry(body)
        }
    }
}
