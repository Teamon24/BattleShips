package org.home.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

private val serverScope = threadsScope(2, "SERVER-POOL")
val ioScope = CoroutineScope(Dispatchers.IO)

fun threadsScope(threads: Int, name: String) =
    CoroutineScope(newFixedThreadPoolContext(nThreads = threads, name))

fun singleThreadScope(name: String) = CoroutineScope(newSingleThreadContext(name))

fun singleThread(name: String = "", launchBlock: suspend () -> Any) = CoroutineScope(newSingleThreadContext(name)).launch { launchBlock() }
