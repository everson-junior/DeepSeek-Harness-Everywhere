using System;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Everywhere.Services;
using Microsoft.VisualStudio.Shell;
using Microsoft.Web.WebView2.Core;

namespace Everywhere.ToolWindows
{
    public partial class EverywhereToolWindowControl : UserControl
    {
        private bool _isWebViewInitialized = false;

        public EverywhereToolWindowControl()
        {
            InitializeComponent();
            Loaded += EverywhereToolWindowControl_Loaded;
            DshManager.Instance.StatusChanged += DshManager_StatusChanged;
        }

        private async void EverywhereToolWindowControl_Loaded(object sender, RoutedEventArgs e)
        {
            await InitWebView2Async();

            var autoStart = EverywherePackage.Instance?.Options?.AutoStart ?? true;
            if (autoStart && DshManager.Instance.Status == HarnessStatus.Stopped)
            {
                await StartHarnessAsync();
            }
            else
            {
                UpdateUI(DshManager.Instance.Status);
            }
        }

        private async System.Threading.Tasks.Task InitWebView2Async()
        {
            if (_isWebViewInitialized) return;

            try
            {
                var localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
                var userDataFolder = Path.Combine(localAppData, "Everywhere", "WebView2Data");
                Directory.CreateDirectory(userDataFolder);

                var env = await CoreWebView2Environment.CreateAsync(null, userDataFolder);
                await WebBrowser.EnsureCoreWebView2Async(env);

                WebBrowser.CoreWebView2.Settings.IsStatusBarEnabled = false;
                WebBrowser.CoreWebView2.Settings.AreDefaultContextMenusEnabled = true;
                WebBrowser.CoreWebView2.Settings.IsScriptEnabled = true;

                _isWebViewInitialized = true;
            }
            catch (Exception ex)
            {
                TxtStatus.Text = "WebView2 Error: " + ex.Message;
            }
        }

        private void DshManager_StatusChanged(object sender, HarnessStatus status)
        {
            Dispatcher.Invoke(() => UpdateUI(status));
        }

        private void UpdateUI(HarnessStatus status)
        {
            switch (status)
            {
                case HarnessStatus.Running:
                    TxtStatus.Text = $"Active (:{DshManager.Instance.ActivePort})";
                    TxtStatus.Foreground = new SolidColorBrush(Color.FromRgb(78, 201, 176));
                    StartOverlay.Visibility = Visibility.Collapsed;
                    if (!string.IsNullOrEmpty(DshManager.Instance.ActiveUrl) && _isWebViewInitialized)
                    {
                        WebBrowser.Source = new Uri(DshManager.Instance.ActiveUrl);
                    }
                    break;

                case HarnessStatus.Starting:
                    TxtStatus.Text = "Starting...";
                    TxtStatus.Foreground = new SolidColorBrush(Color.FromRgb(220, 180, 80));
                    StartOverlay.Visibility = Visibility.Visible;
                    TxtOverlayError.Visibility = Visibility.Collapsed;
                    BtnOverlayStart.IsEnabled = false;
                    BtnOverlayStart.Content = "Iniciando DeepSeek Harness...";
                    break;

                case HarnessStatus.Error:
                    TxtStatus.Text = "Error";
                    TxtStatus.Foreground = new SolidColorBrush(Color.FromRgb(244, 67, 54));
                    StartOverlay.Visibility = Visibility.Visible;
                    TxtOverlayError.Text = DshManager.Instance.LastError ?? "Erro desconhecido ao iniciar o Harness.";
                    TxtOverlayError.Visibility = Visibility.Visible;
                    BtnOverlayStart.IsEnabled = true;
                    BtnOverlayStart.Content = "🔄 Tentar Novamente";
                    break;

                case HarnessStatus.Stopped:
                default:
                    TxtStatus.Text = "Stopped";
                    TxtStatus.Foreground = new SolidColorBrush(Color.FromRgb(136, 136, 136));
                    StartOverlay.Visibility = Visibility.Visible;
                    TxtOverlayError.Visibility = Visibility.Collapsed;
                    BtnOverlayStart.IsEnabled = true;
                    BtnOverlayStart.Content = "▶ Iniciar DeepSeek Harness";
                    break;
            }
        }

        private async System.Threading.Tasks.Task StartHarnessAsync()
        {
            try
            {
                await InitWebView2Async();
                var url = await DshManager.Instance.StartAsync();
                if (_isWebViewInitialized)
                {
                    WebBrowser.Source = new Uri(url);
                }
            }
            catch (Exception ex)
            {
                TxtOverlayError.Text = ex.Message;
                TxtOverlayError.Visibility = Visibility.Visible;
            }
        }

        private async void BtnStart_Click(object sender, RoutedEventArgs e)
        {
            await StartHarnessAsync();
        }

        private async void BtnStop_Click(object sender, RoutedEventArgs e)
        {
            await DshManager.Instance.StopAsync();
        }

        private async void BtnRestart_Click(object sender, RoutedEventArgs e)
        {
            await DshManager.Instance.RestartAsync();
        }

        private void BtnReload_Click(object sender, RoutedEventArgs e)
        {
            if (_isWebViewInitialized && WebBrowser.Source != null)
            {
                WebBrowser.Reload();
            }
        }

        private void BtnOptions_Click(object sender, RoutedEventArgs e)
        {
            ThreadHelper.ThrowIfNotOnUIThread();
            EverywherePackage.Instance?.ShowOptionPage(typeof(Options.EverywhereOptionPage));
        }
    }
}
