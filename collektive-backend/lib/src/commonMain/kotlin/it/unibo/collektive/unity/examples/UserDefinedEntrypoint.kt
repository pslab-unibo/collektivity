package it.unibo.collektive.unity.examples

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.unity.schema.NodeState
import it.unibo.collektive.unity.schema.SensorData
import it.unibo.collektive.unity.shared.Vector3Helper
import it.unibo.collektive.unity.shared.minus
import it.unibo.collektive.unity.shared.times
import it.unibo.collektive.unity.shared.div
import it.unibo.collektive.unity.shared.plus
import kotlin.math.*

fun Aggregate<Int>.entrypoint(sensorData: SensorData): NodeState{
  val neighbors = neighboring(sensorData).neighbors.sequence
  if (neighbors.none())
    return NodeState(sensorData.currentPosition)
  val weightedVectorSum = neighbors.fold(Vector3Helper.zero()) { acc, nbr ->
    val relativeVector = nbr.value.currentPosition - sensorData.currentPosition
    val weight = nbr.value.sourceIntensity.pow(2)
    acc + (relativeVector * weight.toFloat())
  }
  val totalWeight = neighbors.sumOf { it.value.sourceIntensity.pow(2) } + 
      sensorData.sourceIntensity.pow(2)
  val averageDisplacement = if (totalWeight > 0) {
    weightedVectorSum / totalWeight.toFloat()
  } else {
    Vector3Helper.zero()
  }
  return NodeState(sensorData.currentPosition + averageDisplacement)
}
    // NodeState(
    //     neighboring(sensorData)
    //         .neighbors.sequence
    //         .filter { it.value.sourceIntensity > sensorData.sourceIntensity }
    //         .maxByOrNull { it.value.sourceIntensity }?.value
    //         ?.currentPosition ?: sensorData.currentPosition
    // )
