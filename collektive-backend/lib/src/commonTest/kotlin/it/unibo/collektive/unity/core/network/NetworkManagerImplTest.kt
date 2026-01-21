package it.unibo.collektive.unity.core.network

import it.unibo.collektive.networking.NoNeighborsData
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertIs

class NetworkManagerImplTest {

    @Test
    fun testNodeRegistrationAndConnection() {
        val nm = NetworkManagerImpl()
        nm.registerNode(1)
        nm.registerNode(2)
        assertTrue(nm.addConnection(1, 2), "Connection should be added successfully")
        val dataFor1 = nm.receiveMessageFor(1)
        assertTrue(dataFor1.neighbors.contains(2), "Node 1 should see Node 2 as a neighbor")
    }

    @Test
    fun testDoubleConnectionFail() {
        val nm = NetworkManagerImpl()
        nm.registerNode(1)
        nm.registerNode(2)
        nm.addConnection(1,2)
        assertFalse(nm.addConnection(1, 2), "Connection should fail")
    }

    @Test
    fun testUnregisterNodeCleansUpConnections() {
        val nm = NetworkManagerImpl()
        nm.registerNode(1)
        nm.registerNode(2)
        nm.addConnection(1, 2)
        nm.unregisterNode(2)
        val dataFor1 = nm.receiveMessageFor(1)
        assertTrue(dataFor1.neighbors.isEmpty(), "Node 1 should have no neighbors after Node 2 is unregistered")
    }

    @Test
    fun testReceiveMessageWithoutRegistration() {
        val nm = NetworkManagerImpl()
        val result = nm.receiveMessageFor(99)
        assertIs<NoNeighborsData<Int>>(result, "Should return NoNeighborsData for unregistered IDs")
    }
}
