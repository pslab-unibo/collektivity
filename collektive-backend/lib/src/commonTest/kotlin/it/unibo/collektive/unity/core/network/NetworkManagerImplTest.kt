package it.unibo.collektive.unity.core.network

import it.unibo.collektive.aggregate.api.DataSharingMethod
import it.unibo.collektive.aggregate.api.InMemory
import it.unibo.collektive.networking.InMemoryMessage
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.MessageFactory
import it.unibo.collektive.networking.NoNeighborsData
import it.unibo.collektive.networking.OutboundEnvelope
import it.unibo.collektive.path.FullPath
import it.unibo.collektive.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertIs

class NetworkManagerImplTest {

    @Test
    fun testRegisterNode() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        val neighborsData = manager.receiveMessageFor(1)
        assertEquals(emptySet(), neighborsData.neighbors)
    }

    @Test
    fun testRegisterNodeIsIdempotent() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(1)
        val neighborsData = manager.receiveMessageFor(1)
        assertEquals(emptySet(), neighborsData.neighbors)
    }

    @Test
    fun testUnregisterNode() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.unregisterNode(1)
        val neighbors = manager.receiveMessageFor(1)
        assertIs<NoNeighborsData<Int>>(neighbors)
    }

    @Test
    fun testUnregisterNodeRemovesFromNeighborLists() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.subscribe(1, 2)
        manager.unregisterNode(2)
        val neighborsData = manager.receiveMessageFor(1)
        assertFalse(neighborsData.neighbors.contains(2))
    }

    @Test
    fun testUnregisterNodeRemovesOutboxData() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        val envelope = createMockEnvelope(1, emptyMap())
        manager.send(1, envelope)
        manager.unregisterNode(1)
        val neighbors = manager.receiveMessageFor(1)
        assertIs<NoNeighborsData<Int>>(neighbors)
    }

    @Test
    fun testUnregisterNonExistentNode() {
        val manager = NetworkManagerImpl()
        // should not throw
        manager.unregisterNode(999)
    }

    @Test
    fun testSubscribe() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        val added = manager.subscribe(1, 2)
        assertTrue(added)
        assertTrue(manager.receiveMessageFor(1).neighbors.contains(2))
        assertFalse(manager.receiveMessageFor(2).neighbors.contains(1))
    }

    @Test
    fun testAddDuplicateConnectionReturnsFalse() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.subscribe(1, 2)
        val added = manager.subscribe(1, 2)
        assertFalse(added)
    }

    @Test
    fun testBidirectionalConnections() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.subscribe(1, 2)
        manager.subscribe(2, 1)
        assertTrue(manager.receiveMessageFor(1).neighbors.contains(2))
        assertTrue(manager.receiveMessageFor(2).neighbors.contains(1))
    }

    @Test
    fun testUnsubscribe() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.subscribe(1, 2)
        val removed = manager.unsubscribe(1, 2)
        assertTrue(removed)
        assertFalse(manager.receiveMessageFor(1).neighbors.contains(2))
    }

    @Test
    fun testRemoveNonExistentConnectionReturnsFalse() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        val removed = manager.unsubscribe(1, 2)
        assertFalse(removed)
    }

    @Test
    fun testRemoveConnectionWhenNodeDoesNotExist() {
        val manager = NetworkManagerImpl()
        val removed = manager.unsubscribe(1, 2)
        assertFalse(removed)
    }

    @Test
    fun testUnsubscribeOnlyRemovesSpecifiedDirection() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.subscribe(1, 2)
        manager.subscribe(2, 1)
        manager.unsubscribe(1, 2)
        assertFalse(manager.receiveMessageFor(1).neighbors.contains(2))
        assertTrue(manager.receiveMessageFor(2).neighbors.contains(1))
    }

    @Test
    fun testSendAndReceiveMessages() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.subscribe(1, 2)
        val path = mockPath()
        val value = "testValue"
        val sharedData = mapOf(path to value)
        val envelope = createMockEnvelope(2, sharedData)
        manager.send(2, envelope)
        val received = manager.receiveMessageFor(1)
        assertTrue(received.dataAt(path, InMemory).containsValue(value))
    }

    @Test
    fun testReceiveMessageForUnregisteredNode() {
        val manager = NetworkManagerImpl()
        val received = manager.receiveMessageFor(999)
        assertIs<NoNeighborsData<Int>>(received)
    }

    @Test
    fun testReceiveMessageForNodeWithNoConnections() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        val received = manager.receiveMessageFor(1)
        assertTrue(received.neighbors.isEmpty())
    }

    @Test
    fun testReceiveMessageFromMultipleNeighbors() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        manager.registerNode(2)
        manager.registerNode(3)
        manager.subscribe(1, 2)
        manager.subscribe(1, 3)
        val envelope2 = createMockEnvelope(2, emptyMap())
        val envelope3 = createMockEnvelope(3, emptyMap())
        manager.send(2, envelope2)
        manager.send(3, envelope3)
        val received = manager.receiveMessageFor(1)
        assertTrue(received.neighbors.contains(2))
        assertTrue(received.neighbors.contains(3))
    }

    @Test
    fun testSendUpdatesMessage() {
        val manager = NetworkManagerImpl()
        manager.registerNode(1)
        val envelope1 = createMockEnvelope(1, mapOf(mockPath() to "first"))
        manager.send(1, envelope1)
        val envelope2 = createMockEnvelope(1, mapOf(mockPath() to "second"))
        manager.send(1, envelope2)
        manager.registerNode(2)
        manager.subscribe(2, 1)
        val received = manager.receiveMessageFor(2)
        assertTrue(received.neighbors.contains(1))
    }
}

// Mocks
private var pathCounter = 0

private fun mockPath(): Path {
    val id = pathCounter++
    return FullPath(listOf("test-$id"))
}


private class MockOutboundEnvelope(
    private val senderId: Int,
    private val sharedData: Map<Path, Any>
) : OutboundEnvelope<Int> {

    override fun <Value> addData(
        path: Path,
        data: OutboundEnvelope.SharedData<Int, Value>,
        dataSharingMethod: DataSharingMethod<Value>
    ) {
        // No-op
    }

    override fun isEmpty(): Boolean = sharedData.isEmpty()

    override fun isNotEmpty(): Boolean = sharedData.isNotEmpty()

    override fun prepareMessageFor(
        receiverId: Int,
        factory: MessageFactory<Int, *>
    ): Message<Int, Any?> {
        return InMemoryMessage(senderId, sharedData)
    }
}

private fun createMockEnvelope(senderId: Int, sharedData: Map<Path, Any>): OutboundEnvelope<Int> {
    return MockOutboundEnvelope(senderId, sharedData)
}
