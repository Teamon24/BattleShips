package org.home.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

fun threadsScope(threads: Int, name: String = "") =
    CoroutineScope(newFixedThreadPoolContext(nThreads = threads, name))

fun singleThreadScope(name: String) = CoroutineScope(newSingleThreadContext(name))

fun <T> singleThreadScope(name: String = "", launchBlock: suspend () -> T) =
    CoroutineScope(newSingleThreadContext(name)).launch { launchBlock() }

@OptIn(ExperimentalCoroutinesApi::class)
fun ioScope(name: String) = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
