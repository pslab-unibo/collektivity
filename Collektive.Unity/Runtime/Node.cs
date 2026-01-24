using System;
using Collektive.Unity.Attributes;
using Collektive.Unity.Schema;
using UnityEngine;
using Random = System.Random;

namespace Collektive.Unity
{
    /// <summary>
    /// A collektive Node.
    /// </summary>
    public abstract class Node : MonoBehaviour
    {
        [SerializeField, ReadOnly]
        private int id;

        private Random _prng;

        public int Id => id;

        public Action<NodeState> OnStateReceived;

        public abstract SensorData Sense();
        protected abstract void Act(NodeState state);

        protected float GetNextRandom() => (float)_prng.NextDouble();

        protected float GetGaussian(float mean, float stdDev)
        {
            double u1 = 1.0 - _prng.NextDouble();
            double u2 = 1.0 - _prng.NextDouble();
            double randStdNormal = Math.Sqrt(-2.0 * Math.Log(u1)) * Math.Sin(2.0 * Math.PI * u2);
            return mean + stdDev * (float)randStdNormal;
        }

        private void OnEnable()
        {
            OnStateReceived += Act;
            id = SimulationManager.Instance.AddNode(this);
            name = $"node {Id}";
            _prng = _prng == null ? new Random(SimulationManager.Instance.MasterSeed + Id) : _prng;
        }

        private void OnDisable()
        {
            OnStateReceived -= Act;
            SimulationManager.Instance.RemoveNode(this);
        }
    }
}
