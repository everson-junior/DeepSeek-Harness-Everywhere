using System;
using System.Runtime.InteropServices;
using System.Threading;
using Microsoft.VisualStudio.Shell;
using Microsoft.VisualStudio.Shell.Interop;
using Task = System.Threading.Tasks.Task;

namespace Everywhere
{
    [PackageRegistration(UseManagedResourcesOnly = true, AllowsBackgroundLoading = true)]
    [Guid(EverywherePackage.PackageGuidString)]
    [ProvideMenuResource("Menus.ctmenu", 1)]
    [ProvideToolWindow(typeof(ToolWindows.EverywhereToolWindow), Style = VsDockStyle.Tabbed, Window = "34E76E81-EE4A-11D0-AE2E-00A0C90FFFC3")]
    [ProvideOptionPage(typeof(Options.EverywhereOptionPage), "Everywhere", "General", 0, 0, true)]
    [ProvideAutoLoad(UIContextGuids80.NoSolution, PackageAutoLoadFlags.BackgroundLoad)]
    [ProvideAutoLoad(UIContextGuids80.SolutionExists, PackageAutoLoadFlags.BackgroundLoad)]
    public sealed class EverywherePackage : AsyncPackage
    {
        public const string PackageGuidString = "a5323a67-932f-45b7-87dc-4db7eb0b1f20";
        public static EverywherePackage? Instance { get; private set; }

        public Options.EverywhereOptionPage Options => (Options.EverywhereOptionPage)GetDialogPage(typeof(Options.EverywhereOptionPage));

        protected override async Task InitializeAsync(CancellationToken cancellationToken, IProgress<ServiceProgressData> progress)
        {
            await this.JoinableTaskFactory.SwitchToMainThreadAsync(cancellationToken);
            Instance = this;

            await Commands.EverywhereCommands.InitializeAsync(this);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                Services.DshManager.Instance.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
