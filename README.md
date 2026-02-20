# Collektive.Unity

Unity package that enables aggregate simulation through collektive framework with FFI communication

## Requirements

- Unity 6000.3.8f1
- java 21
- npm 11.10.0 and NodeJS v25.6.1

## To launch builtin example

1. clone the repo and open the unity editor pointing at the `collektive.unity/rich-scenario` directory;
1. in the top bar click `Tools` button and then launch both `Proto > Generate` and `Native > Rebuild backend`;
1. from the `project` tab (usually at the bottom of the editor) open the `collektive.unity/rich-scenario/Assets/Scenes/Oasis` directory and double click on the `Oasis.unity` file to open the scene;
1. start the simulation by pressing the start button at the center top of the unity editor.

## To create a new simulation

1. Update the file at `collektive.unity/collektive-backend/lib/src/commonMain/proto/user-defined-schema.proto` with the values you want to share across the bridge Collektive-Unity;
    1. `SensorData` are the data Unity will send to Collektive;
    1. `ActuatorData` are the data Collektive sends back to Unity at each step;
    1. these data should be specific to one node, Collektive.Unity will handle the propagation to every node of the simulation;
1. launch `./gradlew build` inside the `collektive-backend` directory;
    1. this triggers the proto compiler to recompute data structures so that you can use them in your code;
1. update the `collketive.unity/collektive-backend/lib/src/commonMain/kotlin/it/unibo/collektive/unity/examples/UserDefinedEntrypoint.kt` file with the aggregate program you want to simulate;
1. open the unity editor pointing at the `collektive.unity/Sandbox.Collektive.Unity` directory;
1. in the top bar click `Tools` button and then launch both `Proto > Generate` and `Native > Rebuild backend`;
    1. delete or comment files that are using the previous version of the proto-generated classes;
1. create your own node behavior by extending the `Node` class (a Unity `MonoBehaviour`);
    1. implement the abstract methods `Act` and `Sense`;
1. in the unity editor create a new scene and add game objects to the scene;
1. attach to those game objects the new class you've implemented by extending the `Node` class;
1. add the `Simulation Manager` prefab to the scene (can be found at `collektive.unity/Collektive.Unity/Runtime/Prefabs/Simulation Manager.prefab`);
1. create a new `MonoBehaviour` that interact with the `SimulationManager.Instance` singleton APIs to choose a neighborhood logic (see the `ProximityNeighborhoodBehaviour` in the `collektive.unity/Collektive.Unity/Runtime/Example/` directory for an example)
1. start your simulation by pressing the start button at the center top of the unity editor.
