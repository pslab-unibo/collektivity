package it.unibo.collektive.unity.core.network

import it.unibo.collektive.aggregate.api.DataSharingMethod
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.NeighborsData
import it.unibo.collektive.networking.NoNeighborsData
import it.unibo.collektive.networking.OutboundEnvelope
import it.unibo.collektive.path.Path
import it.unibo.collektive.unity.core.Lock

/**
 * Network manager that handles messaging.
 */
interface NetworkManager {

    /**
     * Registers a node in the global network.
     */
    fun registerNode(id: Int)

    /**
     * Unregisters a node in the global network.
     */
    fun unregisterNode(id: Int)

    /**
     * Send the message from local id.
     */
    fun send(local: Int, envelope: OutboundEnvelope<Int>)

    /**
     * Retrieve message for id.
     */
    fun receiveMessageFor(id: Int): NeighborsData<Int>

    /**
     * Subscribe the subscriber to the publisher.
     * @return true if the subscription was successfully done, false otherwise.
     */
    fun subscribe(subscriber: Int, publisher: Int): Boolean

    /**
     * Unsubscribe the subscriber to the publisher.
     * @return true if the unsubscription was successfully done, false otherwise.
     */
    fun unsubscribe(subscriber: Int, publisher: Int): Boolean
}

class NetworkManagerImpl : NetworkManager {

    private val adjacencyMap = mutableMapOf<Int, MutableSet<Int>>()
    private val outboxes = mutableMapOf<Int, OutboundEnvelope<Int>>()
    private val lock = Lock()

    override fun registerNode(id: Int) {
        adjacencyMap.getOrPut(id) { mutableSetOf() }
    }

    override fun unregisterNode(id: Int) {
        outboxes.remove(id)
        adjacencyMap.remove(id)
        adjacencyMap.values
            .filter { it.contains(id) }
            .forEach { it.remove(id) }
    }

    override fun send(local: Int, envelope: OutboundEnvelope<Int>) = lock.withLock {
        outboxes[local] = envelope
    }

    override fun receiveMessageFor(id: Int): NeighborsData<Int> = lock.withLock {
        val neighbors = adjacencyMap[id] ?: return@withLock NoNeighborsData()
        return@withLock NeighborsDataImpl(
            neighbors,
            neighbors.mapNotNull { nbrId ->
                outboxes[nbrId]?.let { envelope ->
                    nbrId to envelope.prepareMessageFor(id)
                }
            }.toMap()
        )
    }

    override fun subscribe(subscriber: Int, publisher: Int): Boolean = lock.withLock {
        return@withLock adjacencyMap.getOrPut(subscriber) { mutableSetOf() }.add(publisher)
    }

    override fun unsubscribe(subscriber: Int, publisher: Int): Boolean = lock.withLock {
        return@withLock adjacencyMap[subscriber]?.remove(publisher) ?: false
    }
}

class NeighborsDataImpl(override val neighbors: Set<Int>, private val inbound: Map<Int, Message<Int, *>>) : NeighborsData<Int>
{
    @Suppress("UNCHECKED_CAST")
    override fun <Value> dataAt(
        path: Path,
        dataSharingMethod: DataSharingMethod<Value>
    ): Map<Int, Value> =
        inbound
            .asSequence()
            .filter { (senderId, _) -> senderId in neighbors }
            .mapNotNull { (senderId, msg) ->
                val raw = msg.sharedData.getOrElse(path) { NoValue } as Value
                if (raw == NoValue) null else senderId to raw
            }
            .toMap()

    private object NoValue
}
