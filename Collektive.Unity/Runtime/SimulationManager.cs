using Collektive.Unity.Data;
using Collektive.Unity.Native;
using Collektive.Unity.Schema;
using UnityEngine;

namespace Collektive.Unity
{
    /// <summary>
    /// Handles the logic part of the simulation.
    /// </summary>
    public class SimulationManager : SingletonBehaviour<SimulationManager>
    {
        [SerializeField]
        private int totalCounts;

        [SerializeField]
        private float deltaTime = 0.02f;

        private void Awake()
        {
            EngineNativeApi.Initialize(
                new GlobalData { TotalNodes = totalCounts, DeltaTime = deltaTime }
            );
        }

        private void FixedUpdate()
        {
            //TODO
        }

        public bool AddConnection(int node1, int node2) =>
            EngineNativeApi.AddConnection(node1, node2);

        public bool RemoveConnection(int node1, int node2) =>
            EngineNativeApi.RemoveConnection(node1, node2);

        public void UpdateGlobalData(CustomGlobalData data) =>
            EngineNativeApi.UpdateGlobalData(data);
    }
}
