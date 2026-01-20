package it.unibo.collektive.unity.core

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual class Lock {
    @OptIn(ExperimentalForeignApi::class)
    private val mutex = nativeHeap.alloc<pthread_mutex_t>()
    init { pthread_mutex_init(mutex.ptr, null) }

    actual fun <T> withLock(block: () -> T): T {
        pthread_mutex_lock(mutex.ptr)
        try { return block() } finally { pthread_mutex_unlock(mutex.ptr) }
    }
}
