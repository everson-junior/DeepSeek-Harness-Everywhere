using System;
using System.Runtime.InteropServices;
using Microsoft.VisualStudio.Shell;

namespace Everywhere.ToolWindows
{
    [Guid("6e57dbf5-9614-4ebc-b356-91e813a893ef")]
    public class EverywhereToolWindow : ToolWindowPane
    {
        public EverywhereToolWindow() : base(null)
        {
            Caption = "Everywhere (DeepSeek)";
            Content = new EverywhereToolWindowControl();
        }
    }
}
