package org.home.utils

import org.home.utils.extensions.AtomicBooleansExtensions.invoke
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

sealed class InfiniteTryBase<E, H> {
    lateinit var condition: AtomicBoolean
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

    abstract fun putNoArgHandler(body: () -> Unit)

    fun getHandler(ex: Exception): H {
        val handlerAndExceptions = handlers.entries.firstOrNull { handlerAndExs ->
            ex::class in handlerAndExs.value
        }

        handlerAndExceptions ?: throw ex

        return handlerAndExceptions.key
    }

    companion object {
        infix fun <E, H> InfiniteTryBase<E, H>.doWhile(condition: AtomicBoolean) {
            this.condition = condition
            while (condition()) { loopBody() }
            log { "i'm done" }
        }

        infix fun <E, H> InfiniteTryBase<E, H>.handle(handler: H) { putHandler(handler) }

        inline infix fun <E, H> InfiniteTryBase<E, H>.catch(function: InfiniteTryBase<E, H>.() -> Unit) =
            apply { function() }

        inline infix fun <E, H> InfiniteTryBase<E, H>.ignore(skippedExClass: () -> KClass<out Exception>) =
            apply {
                +skippedExClass()
                putHandler(emptyHandler)
            }

        inline infix fun <E, H> InfiniteTryBase<E, H>.stopOn(stopExClass: () -> KClass<out Exception>) =
            apply {
                +stopExClass()
                putNoArgHandler { condition(false) }
            }

        inline infix fun <E, H> InfiniteTryBase<E, H>.stopOnAll(stopExClasses: () -> List<KClass<out Exception>>) =
            apply {
                +stopExClasses()
                putNoArgHandler { condition(false) }
            }

    }

    abstract fun loopBody()
}

class InfiniteTryFor<E>(
    val elements: Collection<E>,
    val body: (E) -> Unit): InfiniteTryBase<E, (Exception, E) -> Unit>() {
    override val emptyHandler = { _: Exception, _: E -> }

    override fun loopBody() {
        elements.forEach {
            try { body(it) }
            catch (ex: Exception) { getHandler(ex)(ex, it) }
        }
    }

    companion object {
        infix fun <E> Collection<E>.infiniteTryFor(forEach: (E) -> Unit) = InfiniteTryFor(this, forEach)
    }

    override fun putNoArgHandler(body: () -> Unit) { putHandler { _, _ -> body() } }
}


class InfiniteTry(val body: () -> Unit): InfiniteTryBase<Unit, (Exception) -> Unit>() {
    override val emptyHandler = { _: Exception -> }

    override fun loopBody() {
        try { body() }
        catch (ex: Exception) { getHandler(ex)(ex) }
    }

    companion object {
        fun loop(body: () -> Unit) = InfiniteTry(body)
    }

    override fun putNoArgHandler(body: () -> Unit) { putHandler { body() } }
}
