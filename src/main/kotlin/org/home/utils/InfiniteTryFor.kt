package org.home.utils

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

sealed class InfiniteTryBase<E, H> {
    private val handlers = mutableMapOf<H, List<KClass<out Exception>>>()
    private val exceptions = mutableListOf<KClass<out Exception>>()
    protected var noSignalToStop = true
    abstract val stopHandler: H

    operator fun KClass<out Exception>.unaryPlus() {
        exceptions.add(this)
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
            while (condition.get() && noSignalToStop) { loopBody() }
            log { "i'm done" }
        }

        infix fun <E, H> InfiniteTryBase<E, H>.doWhile(boolean: Boolean) {
            while (boolean && noSignalToStop) { loopBody() }
        }

        infix fun <E, H> InfiniteTryBase<E, H>.handle(b: H) {
            this.putHandler(b)
        }

        inline infix fun <E, H> InfiniteTryBase<E, H>.catch(
            function: InfiniteTryBase<E, H>.() -> Unit
        ): InfiniteTryBase<E, H> {
            this.function()
            return this
        }

        fun <E, H> InfiniteTryBase<E, H>.stopLoop() {
            log { "stop signal for loop" }
            noSignalToStop = false
        }
    }

    abstract fun loopBody()
}

class InfiniteTryFor<E>(
    val elements: Collection<E>,
    val body: (E) -> Unit): InfiniteTryBase<E, (Exception, E) -> Unit>() {
    override val stopHandler = { ex: Exception, e: E -> }

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
    override val stopHandler = { ex: Exception -> }

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
