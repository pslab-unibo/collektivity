package it.unibo.collektive.unity.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class Vector3Test {

    @Test
    fun testDistanceToSamePointIsZero() {
        val v1 = Vector3(1f, 2f, 3f)
        val v2 = Vector3(1f, 2f, 3f)
        assertEquals(0f, v1.distanceTo(v2), "Distance to the same point should be 0")
    }

    @Test
    fun testDistancePositiveCoordinates() {
        val v1 = Vector3(0f, 0f, 0f)
        val v2 = Vector3(3f, 4f, 12f)
        assertEquals(13f, v1.distanceTo(v2), 0.001f)
    }

    @Test
    fun testDistanceNegativeCoordinates() {
        val v1 = Vector3(-1f, -1f, -1f)
        val v2 = Vector3(-4f, -5f, -1f)
        assertEquals(5f, v1.distanceTo(v2), 0.001f)
    }

    @Test
    fun testVector3HelperDistance() {
        val v1 = Vector3(10f, 0f, 0f)
        val v2 = Vector3(5f, 0f, 0f)
        assertEquals(5f, Vector3Helper.distance(v1, v2), "Helper should return same result as extension function")
    }

    @Test
    fun testCommutativeProperty() {
        val v1 = Vector3(1.5f, -2.5f, 3.0f)
        val v2 = Vector3(4.0f, 5.0f, -1.2f)
        assertEquals(v1.distanceTo(v2), v2.distanceTo(v1), "Distance(A,B) must equal Distance(B,A)")
    }
}
