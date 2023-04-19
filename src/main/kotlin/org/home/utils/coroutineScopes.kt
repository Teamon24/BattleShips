package org.home.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

val serverScope = threadsScope(Runtime.getRuntime().availableProcessors(), "SERVER-POOL")
val ioScope = CoroutineScope(Dispatchers.IO)

fun threadsScope(threads: Int, name: String) =
    CoroutineScope(newFixedThreadPoolContext(nThreads = threads, name))

fun singleThreadScope(name: String) = CoroutineScope(newSingleThreadContext(name))

fun singleThreadScope(name: String = "", launchBlock: suspend () -> Any) = CoroutineScope(newSingleThreadContext(name)).launch { launchBlock() }
