using System.Collections;
using System.Collections.Generic;
using System.IO;
using UnityEngine;
using UnityEditor;
using UnityEditor.Android;
using System.Text.RegularExpressions;
using System.Xml;
using UnityEditor.iOS.Xcode;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;

public class AndroidEditor : EditorWindow,IPostGenerateGradleAndroidProject, IPostprocessBuildWithReport
{
    public static string appID = "123123123123";
    public static bool isDebugEnviroment = false;

    private void OnGUI()
    {
        // 从EditorPrefs中获取之前保存的值
        appID = EditorPrefs.GetString("SDKAppID", appID);
        isDebugEnviroment = EditorPrefs.GetBool("SDKIsDebugEnvironment", isDebugEnviroment);

        GUILayout.Label("SDK Settings", EditorStyles.boldLabel);

        // 绘制用于输入 appID 的文本框
        string appid = EditorGUILayout.TextField("App ID:", appID);

        // 绘制用于选择 isDebugEnvironment 的复选框
        bool enviroment = EditorGUILayout.Toggle("Debug Environment:", isDebugEnviroment);

        // 如果值发生了改变，保存新的值到EditorPrefs
        if (appid != appID || enviroment != isDebugEnviroment)
        {
            appID = appid;
            isDebugEnviroment = enviroment;

            EditorPrefs.SetString("SDKAppID", appID);
            EditorPrefs.SetBool("SDKIsDebugEnvironment", isDebugEnviroment);
        }
        if (GUILayout.Button("Save Settings"))
        {
            // 保存设置到 EditorPrefs 中
            EditorPrefs.SetString("SDKAppID", appID);
            EditorPrefs.SetBool("SDKIsDebugEnvironment", isDebugEnviroment);
        }
    }

    [MenuItem("SDKTools/SDKSetting")]
    public static void ShowWindow()
    {
        EditorWindow.GetWindow(typeof(AndroidEditor), false, "Settings");
    }

    public void OnPostGenerateGradleAndroidProject(string path)
    {
        // 获取AndroidManifest.xml的路径
        string manifestPath = Path.Combine(path, "src/main/AndroidManifest.xml");
        string bundelid = "com.DefaultCompany.UnitySDK";//Application.identifier;
        // 读取AndroidManifest.xml文件的内容
        string manifestContent = File.ReadAllText(manifestPath);
        // 在application标签下添加自定义数据
        const string applicationTag = "<application";
        string customData = "android:name=\""+bundelid+".MyApplication\" ";
        manifestContent = manifestContent.Replace(applicationTag, $"{applicationTag} {customData}");
        // 写入修改后的AndroidManifest.xml文件内容
        File.WriteAllText(manifestPath, manifestContent);
        ReWriteActivitySetting(manifestPath);
        // 输出添加自定义数据成功的消息
        Debug.Log("Added custom data to AndroidManifest.xml");
    }

    private void ReWriteActivitySetting(string manifestPath)
    {
        // 加载 AndroidManifest.xml 文件
        XmlDocument doc = new XmlDocument();
        doc.Load(manifestPath);
        // 获取 application 节点
        XmlNode applicationNode = doc.SelectSingleNode("manifest/application");
        // 创建新的元素节点
        if (applicationNode != null)
        {
            XmlElement newElement = doc.CreateElement("meta-data");
            XmlAttribute attributeName = doc.CreateAttribute("android", "name", "http://schemas.android.com/apk/res/android");
            attributeName.Value = "MAIN_ACTIVITY";
            XmlAttribute attributeValue = doc.CreateAttribute("android", "value", "http://schemas.android.com/apk/res/android");
            attributeValue.Value = "true";
            newElement.Attributes.Append(attributeName);
            newElement.Attributes.Append(attributeValue);
            // 将新元素节点添加到 activity 节点
            XmlNodeList activityNodes = applicationNode.SelectNodes("activity");
            if (activityNodes.Count > 0)
            {
                XmlNode activityNode = activityNodes[0];
                activityNode.AppendChild(newElement);
            }


            XmlElement appIDElement = doc.CreateElement("meta-data");
            XmlAttribute appIDElementAttributeName = doc.CreateAttribute("android", "name", "http://schemas.android.com/apk/res/android");
            appIDElementAttributeName.Value = "FB_APP_ID";
            XmlAttribute appIDElementAttributeValue = doc.CreateAttribute("android", "value", "http://schemas.android.com/apk/res/android");
            string realAppID = "\\"+appID;
            appIDElementAttributeValue.Value = realAppID;
            appIDElement.Attributes.Append(appIDElementAttributeName);
            appIDElement.Attributes.Append(appIDElementAttributeValue);
            applicationNode.AppendChild(appIDElement);


            XmlElement ENVElement = doc.CreateElement("meta-data");
            XmlAttribute ENVElementAttributeName = doc.CreateAttribute("android", "name", "http://schemas.android.com/apk/res/android");
            ENVElementAttributeName.Value = "_CGI_PREFIX";
            XmlAttribute ENVElementAttributeValue = doc.CreateAttribute("android", "value", "http://schemas.android.com/apk/res/android");
            ENVElementAttributeValue.Value = "xxxxxxxxx";
            if (!isDebugEnviroment)
            {
                ENVElementAttributeValue.Value = "xxxxbbbbbb";
            }
            ENVElement.Attributes.Append(ENVElementAttributeName);
            ENVElement.Attributes.Append(ENVElementAttributeValue);
            applicationNode.AppendChild(ENVElement);
        }
        else
        {
            Debug.Log("activityNode 没有找到");
        }
        // 保存修改后的 AndroidManifest.xml 文件
        doc.Save(manifestPath);
    }
/// <summary>
/// iOS info.plist设置
/// </summary>
    public int callbackOrder { get { return 0; } }
    public void OnPostprocessBuild(BuildReport report)
    {
 #if UNITY_IPHONE
        string buildPath = report.summary.outputPath;
        // 获取 Info.plist 文件路径
        string plistPath = Path.Combine(buildPath, "Info.plist");
        // 加载 Info.plist 文件
        PlistDocument plist = new PlistDocument();
        plist.ReadFromFile(plistPath);
        // 添加键值对
        PlistElementDict rootDict = plist.root;
        rootDict.SetString("appid", appID);
        plist.WriteToFile(plistPath);
#endif
    }
}
