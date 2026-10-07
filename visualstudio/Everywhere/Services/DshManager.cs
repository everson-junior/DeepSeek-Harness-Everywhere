using System;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using EnvDTE;
using EnvDTE80;
using Microsoft.VisualStudio.Shell;
using Microsoft.VisualStudio.Shell.Interop;
using Process = System.Diagnostics.Process;

namespace Everywhere.Services
{
    public enum HarnessStatus
    {
        Stopped,
        Starting,
        Running,
        Error
    }

    public class DshManager : IDisposable
    {
        private static readonly Lazy<DshManager> _instance = new Lazy<DshManager>(() => new DshManager());
        public static DshManager Instance => _instance.Value;

        private Process? _currentProcess;
        private IVsOutputWindowPane? _outputPane;
        private readonly object _lock = new object();

        public HarnessStatus Status { get; private set; } = HarnessStatus.Stopped;
        public string? ActiveUrl { get; private set; }
        public int? ActivePort { get; private set; }
        public string? LastError { get; private set; }

        public event EventHandler<HarnessStatus>? StatusChanged;

        private DshManager() { }

        private void SetStatus(HarnessStatus newStatus, string? url = null, int? port = null, string? error = null)
        {
            Status = newStatus;
            ActiveUrl = url;
            ActivePort = port;
            LastError = error;
            StatusChanged?.Invoke(this, newStatus);
        }

        private void Log(string message)
        {
            ThreadHelper.ThrowIfNotOnUIThread();
            if (_outputPane == null)
            {
                var outWindow = Package.GetGlobalService(typeof(SVsOutputWindow)) as IVsOutputWindow;
                if (outWindow != null)
                {
                    Guid customGuid = new Guid("9694d93f-67a3-41bb-9467-f4e9a3bdf01a");
                    outWindow.CreatePane(ref customGuid, "Everywhere (DeepSeek Harness)", 1, 1);
                    outWindow.GetPane(ref customGuid, out _outputPane);
                }
            }
            _outputPane?.OutputString($"[Everywhere] {message}\n");
            _outputPane?.Activate();
        }

        public string ResolveWorkspaceDirectory()
        {
            ThreadHelper.ThrowIfNotOnUIThread();
            var dte = Package.GetGlobalService(typeof(DTE)) as DTE2;
            if (dte?.Solution != null && !string.IsNullOrEmpty(dte.Solution.FullName))
            {
                var dir = Path.GetDirectoryName(dte.Solution.FullName);
                if (!string.IsNullOrEmpty(dir) && Directory.Exists(dir))
                {
                    return dir;
                }
            }
            return Environment.CurrentDirectory;
        }

        public string FindDshExecutable(string customPath)
        {
            if (!string.IsNullOrWhiteSpace(customPath) && File.Exists(customPath))
            {
                return customPath;
            }

            var appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            var localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);

            string[] candidates = {
                Path.Combine(appData, "npm", "dsh.cmd"),
                Path.Combine(localAppData, "npm", "dsh.cmd"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".pnpm", "dsh.cmd"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".local", "share", "pnpm", "dsh.cmd"),
                "dsh.cmd",
                "dsh"
            };

            foreach (var path in candidates)
            {
                if (File.Exists(path))
                {
                    return path;
                }
            }

            return "dsh.cmd";
        }

        public async Task<string> StartAsync(bool isRetry = false)
        {
            await ThreadHelper.JoinableTaskFactory.SwitchToMainThreadAsync();

            if (Status == HarnessStatus.Running && !string.IsNullOrEmpty(ActiveUrl))
            {
                return ActiveUrl ?? string.Empty;
            }

            SetStatus(HarnessStatus.Starting);
            Log("Starting DeepSeek Harness runtime...");

            var options = EverywherePackage.Instance?.Options;
            var executable = FindDshExecutable(options?.CustomDshPath ?? "");
            var cwd = ResolveWorkspaceDirectory();
            var port = options?.Port ?? 0;
            var portArg = port > 0 ? $" --port {port}" : "";
            var profileArg = !string.IsNullOrWhiteSpace(options?.Profile) ? options.Profile : "web";
            var arguments = $"--profile {profileArg} --no-open{portArg}";

            var tcs = new TaskCompletionSource<string>();
            var cts = new CancellationTokenSource(TimeSpan.FromSeconds(35));

            cts.Token.Register(() =>
            {
                if (!tcs.Task.IsCompleted)
                {
                    SetStatus(HarnessStatus.Error, error: "Timeout waiting for DeepSeek Harness to start.");
                    tcs.TrySetException(new TimeoutException("Timeout waiting for DeepSeek Harness to start."));
                }
            });

            try
            {
                var startInfo = new ProcessStartInfo
                {
                    FileName = executable,
                    Arguments = arguments,
                    WorkingDirectory = cwd,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };

                if (!string.IsNullOrWhiteSpace(options?.ApiKey))
                {
                    startInfo.EnvironmentVariables["DEEPSEEK_API_KEY"] = options.ApiKey!;
                }
                if (!string.IsNullOrWhiteSpace(options?.BaseUrl))
                {
                    startInfo.EnvironmentVariables["DEEPSEEK_BASE_URL"] = options.BaseUrl!;
                }

                _currentProcess = new Process { StartInfo = startInfo };
                var urlRegex = new Regex(@"dsh web:\s+(https?://[^\s\)]+)", RegexOptions.IgnoreCase);

                _currentProcess.OutputDataReceived += async (s, e) =>
                {
                    if (string.IsNullOrEmpty(e.Data)) return;
                    await ThreadHelper.JoinableTaskFactory.SwitchToMainThreadAsync();
                    Log(e.Data);

                    var match = urlRegex.Match(e.Data);
                    if (match.Success && !tcs.Task.IsCompleted)
                    {
                        var url = match.Groups[1].Value;
                        int port = 3080;
                        try
                        {
                            var uri = new Uri(url);
                            port = uri.Port;
                        }
                        catch { }

                        SetStatus(HarnessStatus.Running, url, port);
                        tcs.TrySetResult(url);
                    }
                };

                _currentProcess.ErrorDataReceived += async (s, e) =>
                {
                    if (string.IsNullOrEmpty(e.Data)) return;
                    await ThreadHelper.JoinableTaskFactory.SwitchToMainThreadAsync();
                    Log(e.Data);
                };

                _currentProcess.EnableRaisingEvents = true;
                _currentProcess.Exited += async (s, e) =>
                {
                    await ThreadHelper.JoinableTaskFactory.SwitchToMainThreadAsync();
                    int exitCode = _currentProcess?.ExitCode ?? -1;
                    Log($"Process exited with code {exitCode}");

                    if (!tcs.Task.IsCompleted)
                    {
                        // Auto-retry on Exit Code 1
                        if (!isRetry && exitCode == 1)
                        {
                            Log("Startup failed with exit code 1. Stopping orphan services and retrying start...");
                            try
                            {
                                await StopAsync();
                                await Task.Delay(1000);
                                var retryUrl = await StartAsync(isRetry: true);
                                tcs.TrySetResult(retryUrl);
                                return;
                            }
                            catch (Exception retryEx)
                            {
                                var retryErrMsg = $"Automatic restart failed: {retryEx.Message}";
                                SetStatus(HarnessStatus.Error, error: retryErrMsg);
                                tcs.TrySetException(new Exception(retryErrMsg));
                                return;
                            }
                        }

                        var errMsg = $"DeepSeek Harness exited with code {exitCode}";
                        SetStatus(HarnessStatus.Error, error: errMsg);
                        tcs.TrySetException(new Exception(errMsg));
                    }
                    else
                    {
                        SetStatus(HarnessStatus.Stopped);
                    }
                };

                _currentProcess.Start();
                _currentProcess.BeginOutputReadLine();
                _currentProcess.BeginErrorReadLine();
            }
            catch (Exception ex)
            {
                SetStatus(HarnessStatus.Error, error: ex.Message);
                tcs.TrySetException(ex);
            }

            return await tcs.Task;
        }

        public async Task StopAsync()
        {
            await ThreadHelper.JoinableTaskFactory.SwitchToMainThreadAsync();
            Log("Stopping DeepSeek Harness and cleaning orphan processes...");

            lock (_lock)
            {
                if (_currentProcess != null && !_currentProcess.HasExited)
                {
                    try
                    {
                        _currentProcess.Kill();
                    }
                    catch { }
                }
                _currentProcess = null;
            }

            // Terminate orphan processes on known DSH ports (3080, 3000 or custom)
            await KillOrphanProcessesAsync();
            SetStatus(HarnessStatus.Stopped);
            Log("DeepSeek Harness stopped.");
        }

        public async Task RestartAsync()
        {
            await StopAsync();
            await Task.Delay(1000);
            await StartAsync();
        }

        private async Task KillOrphanProcessesAsync()
        {
            await Task.Run(() =>
            {
                try
                {
                    // Kill any orphan dsh processes on Windows
                    var killProc = new ProcessStartInfo
                    {
                        FileName = "taskkill.exe",
                        Arguments = "/F /IM dsh.exe /T",
                        CreateNoWindow = true,
                        UseShellExecute = false
                    };
                    using (var p = Process.Start(killProc))
                    {
                        p?.WaitForExit(2000);
                    }
                }
                catch { }

                int[] targetPorts = { ActivePort ?? 3080, 3080, 3000 };
                foreach (var port in targetPorts)
                {
                    if (port > 0)
                    {
                        KillProcessOnPort(port);
                    }
                }
            });
        }

        private void KillProcessOnPort(int port)
        {
            try
            {
                var netstat = new ProcessStartInfo
                {
                    FileName = "netstat.exe",
                    Arguments = "-ano",
                    CreateNoWindow = true,
                    UseShellExecute = false,
                    RedirectStandardOutput = true
                };

                using var p = Process.Start(netstat);
                if (p == null) return;
                var output = p.StandardOutput.ReadToEnd();
                p.WaitForExit(2000);

                var regex = new Regex($@"(?:127\.0\.0\.1|0\.0\.0\.0):{port}\s+.*LISTENING\s+(\d+)", RegexOptions.IgnoreCase);
                var match = regex.Match(output);
                if (match.Success)
                {
                    var pid = match.Groups[1].Value;
                    var kill = new ProcessStartInfo
                    {
                        FileName = "taskkill.exe",
                        Arguments = $"/PID {pid} /T /F",
                        CreateNoWindow = true,
                        UseShellExecute = false
                    };
                    using var killProc = Process.Start(kill);
                    killProc?.WaitForExit(2000);
                }
            }
            catch { }
        }

        public void Dispose()
        {
            lock (_lock)
            {
                if (_currentProcess != null && !_currentProcess.HasExited)
                {
                    try
                    {
                        _currentProcess.Kill();
                    }
                    catch { }
                }
                _currentProcess = null;
            }
        }
    }
}
