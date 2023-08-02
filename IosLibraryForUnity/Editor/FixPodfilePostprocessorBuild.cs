using System.IO;
using UnityEditor;
using UnityEditor.Callbacks;

namespace Game.Editor
{
	public class FixPodfilePostprocessorBuild
	{
		const string FixPodfilePath = "Assets/Editor/Builder/FixPodfile";
		[PostProcessBuild(45)]
		private static void OnPostProcessBuild(BuildTarget target, string pathToBuildProject)
		{
			if (target != BuildTarget.iOS)
			{
				return;
			}
			string buildPath = Path.GetFullPath(pathToBuildProject);
			string podfilePath = Path.Combine(buildPath, "Podfile");
			string content = File.ReadAllText(podfilePath);
			string fixContent = File.ReadAllText(FixPodfilePath);
			content += "\n" + fixContent;
			File.WriteAllText(podfilePath, content);
		}
	}
}