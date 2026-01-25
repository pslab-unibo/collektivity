package it.unibo.collektive.unity.core

/**
 * A lock class to synchronize inside network.
 */
expect class Lock() {
    fun <T> withLock(block: () -> T): T
}