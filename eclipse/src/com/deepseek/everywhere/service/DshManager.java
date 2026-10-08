package com.deepseek.everywhere.service;

import com.deepseek.everywhere.Activator;
import com.deepseek.everywhere.console.EverywhereConsole;
import com.deepseek.everywhere.model.HarnessStatus;
import com.deepseek.everywhere.model.HarnessStatusListener;
import com.deepseek.everywhere.preferences.PreferenceConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Gerenciador autônomo do processo DeepSeek Harness (`dsh`) para o Eclipse IDE.
 * Fornece ciclo de vida, auto-recuperação (exit code 1), detecção de URL e monitoramento.
 */
public class DshManager {

    private static final DshManager INSTANCE = new DshManager();

    private Process currentProcess;
    private final List<HarnessStatusListener> listeners = new CopyOnWriteArrayList<>();

    private volatile HarnessStatus status = HarnessStatus.STOPPED;
    private volatile String activeUrl = null;
    private volatile Integer activePort = null;
    private volatile String lastError = null;

    private static final Pattern URL_DSH_REGEX = Pattern.compile("dsh web:\\s*(https?://\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_FALLBACK_REGEX = Pattern.compile("(https?://(?:127\\.0\\.0\\.1|localhost):\\d+[^\\s\"'<>\\)]*)", Pattern.CASE_INSENSITIVE);

    private DshManager() {}

    public static DshManager getInstance() {
        return INSTANCE;
    }

    public HarnessStatus getStatus() {
        return status;
    }

    public String getActiveUrl() {
        return activeUrl;
    }

    public Integer getActivePort() {
        return activePort;
    }

    public String getLastError() {
        return lastError;
    }

    public void addListener(HarnessStatusListener listener) {
        listeners.add(listener);
        listener.onStatusChanged(status, activeUrl, activePort, lastError);
    }

    public void removeListener(HarnessStatusListener listener) {
        listeners.remove(listener);
    }

    private void setStatus(HarnessStatus newStatus, String url, Integer port, String error) {
        this.status = newStatus;
        this.activeUrl = url;
        this.activePort = port;
        this.lastError = error;
        for (HarnessStatusListener l : listeners) {
            try {
                l.onStatusChanged(newStatus, url, port, error);
            } catch (Exception ignored) {
            }
        }
    }

    public String resolveWorkspaceDirectory() {
        try {
            // 1. Tentar obter projeto a partir da seleção ativa
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    ISelection selection = page.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        Object first = ((IStructuredSelection) selection).getFirstElement();
                        if (first instanceof IResource) {
                            IProject proj = ((IResource) first).getProject();
                            if (proj != null && proj.getLocation() != null) {
                                return proj.getLocation().toFile().getAbsolutePath();
                            }
                        }
                    }
                }
            }

            // 2. Tentar primeiro projeto aberto no workspace
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            if (workspace != null && workspace.getRoot() != null) {
                IProject[] projects = workspace.getRoot().getProjects();
                for (IProject p : projects) {
                    if (p.isOpen() && p.getLocation() != null) {
                        return p.getLocation().toFile().getAbsolutePath();
                    }
                }
                if (workspace.getRoot().getLocation() != null) {
                    return workspace.getRoot().getLocation().toFile().getAbsolutePath();
                }
            }
        } catch (Exception ignored) {
        }

        String userDir = System.getProperty("user.dir");
        if (userDir != null && !userDir.isEmpty()) {
            return userDir;
        }
        return ".";
    }

    public String findDshExecutable(String customPath) {
        if (customPath != null && !customPath.trim().isEmpty()) {
            File f = new File(customPath.trim());
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String userHome = System.getProperty("user.home", "");

        if (isWindows) {
            String appData = System.getenv("APPDATA");
            String localAppData = System.getenv("LOCALAPPDATA");

            List<File> candidates = Arrays.asList(
                    new File(appData != null ? appData : "", "npm/dsh.cmd"),
                    new File(localAppData != null ? localAppData : "", "npm/dsh.cmd"),
                    new File(userHome, ".pnpm/dsh.cmd"),
                    new File(userHome, ".local/share/pnpm/dsh.cmd")
            );

            for (File c : candidates) {
                if (c.exists()) {
                    return c.getAbsolutePath();
                }
            }

            for (String cmd : Arrays.asList("dsh.cmd", "dsh")) {
                try {
                    Process proc = new ProcessBuilder("where.exe", cmd).start();
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                        String line = r.readLine();
                        if (proc.waitFor(2, TimeUnit.SECONDS) && line != null && !line.trim().isEmpty()) {
                            File f = new File(line.trim());
                            if (f.exists()) return f.getAbsolutePath();
                        }
                    }
                } catch (Exception ignored) {}
            }

            return "dsh.cmd";
        } else {
            // Linux / macOS
            File nvmDir = new File(userHome, ".nvm/versions/node");
            if (nvmDir.exists() && nvmDir.isDirectory()) {
                File[] list = nvmDir.listFiles();
                if (list != null) {
                    Arrays.sort(list, (a, b) -> b.getName().compareTo(a.getName()));
                    for (File v : list) {
                        File candidate = new File(v, "bin/dsh");
                        if (candidate.exists()) {
                            return candidate.getAbsolutePath();
                        }
                    }
                }
            }

            List<String> unixCandidates = Arrays.asList(
                    "/home/linux/.npm-global/bin/dsh",
                    userHome + "/.npm-global/bin/dsh",
                    userHome + "/.local/share/pnpm/dsh",
                    userHome + "/.local/bin/dsh",
                    "/usr/local/bin/dsh",
                    "/usr/bin/dsh"
            );

            for (String path : unixCandidates) {
                File c = new File(path);
                if (c.exists()) {
                    return c.getAbsolutePath();
                }
            }

            try {
                Process proc = new ProcessBuilder("which", "dsh").start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line = r.readLine();
                    if (proc.waitFor(2, TimeUnit.SECONDS) && line != null && !line.trim().isEmpty()) {
                        File f = new File(line.trim());
                        if (f.exists()) return f.getAbsolutePath();
                    }
                }
            } catch (Exception ignored) {}

            return "dsh";
        }
    }

    private String extractUrlFromLine(String text) {
        Matcher m1 = URL_DSH_REGEX.matcher(text);
        if (m1.find()) {
            return m1.group(1).trim();
        }
        Matcher m2 = URL_FALLBACK_REGEX.matcher(text);
        if (m2.find()) {
            return m2.group(1).trim();
        }
        return null;
    }

    public synchronized void start() {
        start(false);
    }

    public synchronized void start(final boolean isRetry) {
        if (status == HarnessStatus.RUNNING && activeUrl != null) {
            EverywhereConsole.log("Everywhere já está em execução: " + activeUrl);
            return;
        }

        setStatus(HarnessStatus.STARTING, null, null, null);
        EverywhereConsole.log("Iniciando DeepSeek Harness runtime (tentativa " + (isRetry ? "2/recuperação" : "1") + ")...");

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        final String customDshPath = store.getString(PreferenceConstants.P_CUSTOM_DSH_PATH);
        final String executable = findDshExecutable(customDshPath);
        final String workingDir = resolveWorkspaceDirectory();
        String prof = store.getString(PreferenceConstants.P_PROFILE);
        final String profile = (prof == null || prof.trim().isEmpty()) ? "web" : prof.trim();
        final int targetPort = store.getInt(PreferenceConstants.P_PORT);
        final String apiKey = store.getString(PreferenceConstants.P_API_KEY);
        final String baseUrl = store.getString(PreferenceConstants.P_BASE_URL);

        final List<String> cmdList = new ArrayList<>();
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
        if (isWindows && (executable.toLowerCase().endsWith(".cmd") ||
                executable.toLowerCase().endsWith(".bat") ||
                "dsh".equalsIgnoreCase(executable) ||
                "dsh.cmd".equalsIgnoreCase(executable))) {
            cmdList.add("cmd.exe");
            cmdList.add("/c");
            cmdList.add(executable);
        } else {
            cmdList.add(executable);
        }
        cmdList.add("--profile");
        cmdList.add(profile);
        cmdList.add("--no-open");
        if (targetPort > 0) {
            cmdList.add("--port");
            cmdList.add(String.valueOf(targetPort));
        }

        EverywhereConsole.log("Executável: " + executable);
        EverywhereConsole.log("Diretório de trabalho: " + workingDir);
        EverywhereConsole.log("Comando: " + String.join(" ", cmdList));

        Thread runnerThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmdList);
                pb.directory(new File(workingDir));

                // Configurar variáveis de ambiente
                java.util.Map<String, String> env = pb.environment();
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    env.put("DEEPSEEK_API_KEY", apiKey.trim());
                }
                if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                    env.put("DEEPSEEK_BASE_URL", baseUrl.trim());
                }

                // Expandir PATH para localizar Node.js / npm / pnpm
                String pathKey = env.containsKey("Path") ? "Path" : "PATH";
                String currentPath = env.getOrDefault(pathKey, "");
                String userHome = System.getProperty("user.home", "");
                List<String> extraDirs = new ArrayList<>();
                if (isWindows) {
                    String appData = System.getenv("APPDATA");
                    String localAppData = System.getenv("LOCALAPPDATA");
                    String progFiles = System.getenv("ProgramFiles");
                    if (appData != null) extraDirs.add(appData + "\\npm");
                    if (localAppData != null) extraDirs.add(localAppData + "\\npm");
                    if (progFiles != null) extraDirs.add(progFiles + "\\nodejs");
                    extraDirs.add(userHome + "\\.pnpm");
                } else {
                    extraDirs.add(userHome + "/.npm-global/bin");
                    extraDirs.add(userHome + "/.local/share/pnpm");
                    extraDirs.add(userHome + "/.local/bin");
                    extraDirs.add("/usr/local/bin");
                    extraDirs.add("/usr/bin");
                }
                StringBuilder sbPath = new StringBuilder();
                for (String ed : extraDirs) {
                    if (new File(ed).exists() && !currentPath.contains(ed)) {
                        sbPath.append(ed).append(File.pathSeparator);
                    }
                }
                sbPath.append(currentPath);
                env.put(pathKey, sbPath.toString());

                Process process = pb.start();
                currentProcess = process;
                final AtomicBoolean urlFound = new AtomicBoolean(false);

                // 1. Thread de leitura STDOUT
                Thread stdoutThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            EverywhereConsole.log(line);
                            if (!urlFound.get()) {
                                String detected = extractUrlFromLine(line);
                                if (detected != null && urlFound.compareAndSet(false, true)) {
                                    int detectedPort = 3080;
                                    try {
                                        URI uri = new URI(detected);
                                        if (uri.getPort() > 0) detectedPort = uri.getPort();
                                    } catch (Exception ignored) {}

                                    EverywhereConsole.log("URL do DeepSeek Harness detectada: " + detected);
                                    setStatus(HarnessStatus.RUNNING, detected, detectedPort, null);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                });
                stdoutThread.setDaemon(true);
                stdoutThread.start();

                // 2. Thread de leitura STDERR
                Thread stderrThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            EverywhereConsole.error(line);
                        }
                    } catch (Exception ignored) {}
                });
                stderrThread.setDaemon(true);
                stderrThread.start();

                // 3. Health check paralelo HTTP
                Thread healthThread = new Thread(() -> {
                    List<Integer> portsToCheck = new ArrayList<>();
                    if (targetPort > 0) portsToCheck.add(targetPort);
                    if (!portsToCheck.contains(3080)) portsToCheck.add(3080);
                    if (!portsToCheck.contains(3000)) portsToCheck.add(3000);

                    int attempts = 0;
                    while (!urlFound.get() && attempts < 35 && status == HarnessStatus.STARTING && process.isAlive()) {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            break;
                        }
                        attempts++;
                        for (int p : portsToCheck) {
                            try {
                                URL u = new URL("http://127.0.0.1:" + p + "/");
                                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                                conn.setConnectTimeout(500);
                                conn.setReadTimeout(500);
                                conn.setInstanceFollowRedirects(false);
                                int code = conn.getResponseCode();
                                if (code >= 200 && code < 400) {
                                    if (urlFound.compareAndSet(false, true)) {
                                        String healthUrl = "http://127.0.0.1:" + p + "/";
                                        EverywhereConsole.log("Health check ativo detectou serviço respondendo na porta " + p + " (HTTP " + code + ")");
                                        setStatus(HarnessStatus.RUNNING, healthUrl, p, null);
                                        break;
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                });
                healthThread.setDaemon(true);
                healthThread.start();

                // 4. Timeout watchdog thread (30 segundos)
                Thread watchdogThread = new Thread(() -> {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ignored) {}
                    if (!urlFound.get() && status == HarnessStatus.STARTING && process.isAlive()) {
                        String errMsg = "Tempo limite excedido (30s) aguardando o DeepSeek Harness responder.";
                        EverywhereConsole.error(errMsg);
                        setStatus(HarnessStatus.ERROR, null, null, errMsg);
                    }
                });
                watchdogThread.setDaemon(true);
                watchdogThread.start();

                int exitCode = process.waitFor();
                EverywhereConsole.log("Processo DeepSeek Harness finalizado com código " + exitCode + ".");

                if (!urlFound.get()) {
                    if (!isRetry && exitCode == 1) {
                        EverywhereConsole.log("Falha com código 1 (porta possivelmente ocupada). Limpando serviços órfãos e tentando reiniciar...");
                        stop(true);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}
                        start(true);
                        return;
                    }

                    String errMsg = "DeepSeek Harness encerrou prematuramente com código " + exitCode + ".";
                    setStatus(HarnessStatus.ERROR, null, null, errMsg);
                } else {
                    setStatus(HarnessStatus.STOPPED, null, null, null);
                }

            } catch (Exception e) {
                String errMsg = "Erro ao executar DeepSeek Harness: " + e.getMessage();
                EverywhereConsole.error(errMsg);
                setStatus(HarnessStatus.ERROR, null, null, errMsg);
            }
        }, "Everywhere-DshRunner");

        runnerThread.setDaemon(true);
        runnerThread.start();
    }

    public synchronized void stop() {
        stop(false);
    }

    public synchronized void stop(boolean silent) {
        if (!silent) {
            EverywhereConsole.log("Interrompendo DeepSeek Harness e limpando processos...");
        }

        if (currentProcess != null) {
            try {
                currentProcess.destroy();
                if (!currentProcess.waitFor(2, TimeUnit.SECONDS)) {
                    currentProcess.destroyForcibly();
                }
            } catch (Exception ignored) {}
            currentProcess = null;
        }

        killOrphanProcesses();
        setStatus(HarnessStatus.STOPPED, null, null, null);

        if (!silent) {
            EverywhereConsole.log("DeepSeek Harness interrompido com sucesso.");
        }
    }

    public void restart() {
        stop(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        start(false);
    }

    private void killOrphanProcesses() {
        try {
            boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
            if (isWindows) {
                try {
                    Process p = new ProcessBuilder("taskkill.exe", "/F", "/IM", "dsh.exe", "/T").start();
                    p.waitFor(2, TimeUnit.SECONDS);
                } catch (Exception ignored) {}

                List<Integer> ports = new ArrayList<>();
                if (activePort != null) ports.add(activePort);
                if (!ports.contains(3080)) ports.add(3080);
                if (!ports.contains(3000)) ports.add(3000);
                for (int port : ports) {
                    killWindowsPort(port);
                }
            } else {
                try {
                    Process p = new ProcessBuilder("pkill", "-f", "dsh").start();
                    p.waitFor(2, TimeUnit.SECONDS);
                } catch (Exception ignored) {}

                List<Integer> ports = new ArrayList<>();
                if (activePort != null) ports.add(activePort);
                if (!ports.contains(3080)) ports.add(3080);
                if (!ports.contains(3000)) ports.add(3000);
                for (int port : ports) {
                    try {
                        Process p = new ProcessBuilder("fuser", "-k", port + "/tcp").start();
                        p.waitFor(2, TimeUnit.SECONDS);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    private void killWindowsPort(int port) {
        try {
            Process netstat = new ProcessBuilder("netstat.exe", "-ano").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(netstat.getInputStream()))) {
                Pattern regex = Pattern.compile("(?:127\\.0\\.0\\.1|0\\.0\\.0\\.0):" + port + "\\s+.*LISTENING\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher m = regex.matcher(line);
                    if (m.find()) {
                        String pid = m.group(1);
                        Process kill = new ProcessBuilder("taskkill.exe", "/PID", pid, "/T", "/F").start();
                        kill.waitFor(2, TimeUnit.SECONDS);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
