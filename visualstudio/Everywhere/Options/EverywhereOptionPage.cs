using System.ComponentModel;
using System.Runtime.InteropServices;
using Microsoft.VisualStudio.Shell;

namespace Everywhere.Options
{
    [Guid("470559a1-cb9b-4f92-933e-e673f47e24a7")]
    public class EverywhereOptionPage : DialogPage
    {
        [Category("DeepSeek Harness")]
        [DisplayName("Auto Start")]
        [Description("Automatically start DeepSeek Harness when the tool window is opened.")]
        public bool AutoStart { get; set; } = true;

        [Category("DeepSeek Harness")]
        [DisplayName("Port")]
        [Description("Port for DeepSeek Harness Web server (0 for auto-assign).")]
        public int Port { get; set; } = 0;

        [Category("DeepSeek Harness")]
        [DisplayName("Profile")]
        [Description("DeepSeek Harness profile to boot (e.g. 'web').")]
        public string Profile { get; set; } = "web";

        [Category("DeepSeek Harness")]
        [DisplayName("Custom DSH Path")]
        [Description("Custom command or path to dsh executable. If empty, auto-detects from PATH or npm.")]
        public string CustomDshPath { get; set; } = "";

        [Category("DeepSeek API")]
        [DisplayName("API Key")]
        [Description("DeepSeek API Key. If empty, reads DEEPSEEK_API_KEY environment variable.")]
        [PasswordPropertyText(true)]
        public string ApiKey { get; set; } = "";

        [Category("DeepSeek API")]
        [DisplayName("Base URL")]
        [Description("DeepSeek API Base URL.")]
        public string BaseUrl { get; set; } = "https://api.deepseek.com";
    }
}
