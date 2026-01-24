using System.Collections.Generic;
using System.Linq;
using Collektive.Unity.Attributes;
using Collektive.Unity.Data;
using Collektive.Unity.Native;
using Collektive.Unity.Schema;
using UnityEngine;

namespace Collektive.Unity
{
    /// <summary>
    /// Handles the logic part of the simulation.
    /// </summary>
    [RequireComponent(typeof(LinkManager))]
    public class SimulationManager : SingletonBehaviour<SimulationManager>
    {
        [SerializeField, ReadOnly, Tooltip("The total number of nodes in the simulation")]
        private int totalNodes;

        [SerializeField, Tooltip("Period that passes between one cycle and the next one")]
        private float deltaTime = 0.02f;

        [SerializeField, Tooltip("Set to true to pause global simulation")]
        private bool globalSimulationPaused = false;

        [SerializeField, Tooltip("The master seed from which every random generator begins")]
        private int masterSeed = 42;

        private LinkManager _linkManager;
        private Dictionary<int, Node> _nodes = new();

        public GlobalData GlobalData { get; private set; }
        public int MasterSeed => masterSeed;

        private void Awake()
        {
            _linkManager = GetComponent<LinkManager>();
            GlobalData = new GlobalData { DeltaTime = deltaTime };
            EngineNativeApi.Initialize(GlobalData);
            Physics.simulationMode = SimulationMode.Script;
            Time.timeScale = 0f;
        }

        private void Update()
        {
            if (globalSimulationPaused)
                return;
            foreach (var node in _nodes.Values)
            {
                var sensing = node.Sense();
                var state = EngineNativeApi.Step(node.Id, sensing);
                node.OnStateReceived?.Invoke(state);
            }
            Physics.Simulate(deltaTime);
        }

        public bool AddConnection(Node node1, Node node2)
        {
            _linkManager.AddConnection(node1, node2);
            return EngineNativeApi.AddConnection(node1.Id, node2.Id);
        }

        public bool RemoveConnection(Node node1, Node node2)
        {
            _linkManager.RemoveConnection(node1, node2);
            return EngineNativeApi.RemoveConnection(node1.Id, node2.Id);
        }

        public int AddNode(Node node)
        {
            var id = GetValidId();
            Debug.Log($"Adding node {id}");
            _nodes[id] = node;
            EngineNativeApi.AddNode(id);
            return id;
        }

        public bool RemoveNode(Node node)
        {
            _nodes.Remove(node.Id);
            return EngineNativeApi.RemoveNode(node.Id);
        }

        public void UpdateGlobalData(CustomGlobalData data) =>
            EngineNativeApi.UpdateGlobalData(data);

        private int GetValidId()
        {
            var id = 0;
            while (_nodes.Keys.Contains(id))
                id++;
            return id;
        }
    }
}
