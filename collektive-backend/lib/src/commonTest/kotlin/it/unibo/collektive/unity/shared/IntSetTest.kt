package it.unibo.collektive.unity.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntSetTest {

    @Test
    fun testToSetWithMultipleValues() {
        val intSetProto = IntSet(values = listOf(1, 2, 3, 3, 2, 1))
        val result = intSetProto.ToSet()
        assertEquals(3, result.size, "Set should filter out duplicate values")
        assertTrue(result.containsAll(listOf(1, 2, 3)))
    }

    @Test
    fun testToSetEmpty() {
        val emptyProto = IntSet(values = emptyList())
        val result = emptyProto.ToSet()
        assertTrue(result.isEmpty(), "Conversion of empty list should result in empty set")
    }

    @Test
    fun testToSetPreservesValues() {
        val values = listOf(42, -1, 100)
        val intSetProto = IntSet(values = values)
        val result = intSetProto.ToSet()
        for (value in values) {
            assertTrue(result.contains(value), "Set must contain $value")
        }
    }
}
