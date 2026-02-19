using Collektive.Unity.Schema;

namespace Collektive.Unity.Native
{
    public interface IEngine
    {
        bool Subscribe(int node1, int node2);

        bool Unsubscribe(int node1, int node2);

        bool AddNode(int id);

        bool RemoveNode(int id);

        void Initialize();

        NodeState Step(int id, SensorData sensingData);
    }
}
