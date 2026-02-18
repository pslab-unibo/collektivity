package it.unibo.collektive.unity.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.windows.* // Windows-specific platform headers

@OptIn(ExperimentalForeignApi::class)
actual class Lock {
    private val criticalSection = nativeHeap.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(criticalSection.ptr)
    }

    actual fun <T> withLock(block: () -> T): T {
        EnterCriticalSection(criticalSection.ptr)
        try {
            return block()
        } finally {
            LeaveCriticalSection(criticalSection.ptr)
        }
    }
}
