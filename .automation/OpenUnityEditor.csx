#load "Services/UnityService.csx"
#load "Models/ProjectConfig.csx"

UnityService.OpenProject(new ProjectConfig { Namespace = GetNamespace() });

string GetNamespace() =>
    string.Join(".", Path.GetFileName(Directory.EnumerateDirectories(".", "Sandbox.*").First()).Split(".").Skip(1));
