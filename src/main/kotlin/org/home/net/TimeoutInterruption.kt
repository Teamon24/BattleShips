package org.home.net

import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class TimeoutInterruption<R>(
    private val timeoutMillis: Long,
    private val method: () -> R,
) {

    fun run(): R? {
        val actualFuture = executor.submit(method)
        executor.schedule(canceling(actualFuture), timeoutMillis, TimeUnit.MILLISECONDS)
        return getOrThrow(actualFuture)
    }

    private fun canceling(actualFuture: Future<R>): () -> Unit = {
        if (!actualFuture.isDone) {
            actualFuture.cancel(false)
        }
    }

    private fun getOrThrow(actualFuture: Future<R>): R? {
        var result: R? = null
        try {
            val start = System.nanoTime()
            result = actualFuture.get()
            val finish = System.nanoTime()

            val diff = finish - start
            val millis = diff / 1000000
            val nanos = diff - millis
            println(ANSI_GREEN +
                    "Task was executed within timeout ($timeoutMillis ms): " +
                    "$millis:$nanos" +
                    ANSI_RESET)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: CancellationException) {
            val message = String.format(
                "${ANSI_RED}Timeout (%s ms) was exceeded.$ANSI_RESET",
                timeoutMillis
            )
            println(message)
            return null
        }


        return result
    }

    companion object {
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_RED = "\u001B[31m"

        private val executor = Executors.newScheduledThreadPool(2)

        fun <R> interruptAfter(timeoutMillis: Long, method: () -> R): R? {
            return TimeoutInterruption(timeoutMillis, method).run()
        }
    }
}