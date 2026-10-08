package com.deepseek.everywhere.ui;

import com.deepseek.everywhere.Activator;
import com.deepseek.everywhere.actions.OpenInBrowserAction;
import com.deepseek.everywhere.actions.OpenPreferencesAction;
import com.deepseek.everywhere.actions.ReloadViewAction;
import com.deepseek.everywhere.actions.RestartHarnessAction;
import com.deepseek.everywhere.actions.StartHarnessAction;
import com.deepseek.everywhere.actions.StopHarnessAction;
import com.deepseek.everywhere.console.EverywhereConsole;
import com.deepseek.everywhere.model.HarnessStatus;
import com.deepseek.everywhere.model.HarnessStatusListener;
import com.deepseek.everywhere.preferences.PreferenceConstants;
import com.deepseek.everywhere.service.DshManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * Visualização principal do Everywhere no Eclipse IDE.
 * Incorpora o navegador nativo SWT para exibir o DeepSeek Harness e painel de controle.
 */
public class EverywhereView extends ViewPart implements HarnessStatusListener {

    public static final String ID = "com.deepseek.everywhere.ui.EverywhereView";

    private Composite mainContainer;
    private StackLayout stackLayout;
    private Composite overlayComposite;
    private Composite browserComposite;
    private Browser browser;

    private Label statusLabel;
    private Label workspaceLabel;
    private Label errorLabel;
    private Button startButton;
    private Button stopButton;

    private StartHarnessAction startAction;
    private StopHarnessAction stopAction;
    private RestartHarnessAction restartAction;
    private ReloadViewAction reloadAction;
    private OpenInBrowserAction openInBrowserAction;
    private OpenPreferencesAction openPreferencesAction;

    private Font titleFont;
    private Font subtitleFont;
    private Font boldFont;

    @Override
    public void createPartControl(Composite parent) {
        Display display = parent.getDisplay();
        Font initialFont = parent.getFont();
        FontData[] fd = initialFont.getFontData();
        String fontName = fd.length > 0 ? fd[0].getName() : "Arial";

        titleFont = new Font(display, fontName, 18, SWT.BOLD);
        subtitleFont = new Font(display, fontName, 10, SWT.NORMAL);
        boldFont = new Font(display, fontName, 10, SWT.BOLD);

        mainContainer = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        mainContainer.setLayout(stackLayout);

        createOverlayComposite(mainContainer);
        createBrowserComposite(mainContainer);

        createActions();
        contributeToActionBars();

        DshManager.getInstance().addListener(this);

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        int targetPort = store.getInt(PreferenceConstants.P_PORT);
        DshManager.RunningServiceInfo existing = DshManager.findRunningDsh(targetPort);
        if (existing != null) {
            display.asyncExec(() -> DshManager.getInstance().start());
        } else {
            boolean autoStart = store.getBoolean(PreferenceConstants.P_AUTO_START);
            if (autoStart && DshManager.getInstance().getStatus() == HarnessStatus.STOPPED) {
                display.asyncExec(() -> DshManager.getInstance().start());
            } else {
                stackLayout.topControl = overlayComposite;
                mainContainer.layout();
            }
        }
    }

    private void createOverlayComposite(Composite parent) {
        overlayComposite = new Composite(parent, SWT.NONE);
        GridLayout grid = new GridLayout(1, false);
        grid.marginWidth = 24;
        grid.marginHeight = 28;
        grid.verticalSpacing = 12;
        overlayComposite.setLayout(grid);

        Label title = new Label(overlayComposite, SWT.CENTER);
        title.setText("🌐 Everywhere");
        title.setFont(titleFont);
        title.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        Label subtitle = new Label(overlayComposite, SWT.CENTER);
        subtitle.setText("DeepSeek Harness Autonomous AI Agent");
        subtitle.setFont(subtitleFont);
        subtitle.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        subtitle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        workspaceLabel = new Label(overlayComposite, SWT.CENTER | SWT.WRAP);
        String ws = DshManager.getInstance().resolveWorkspaceDirectory();
        workspaceLabel.setText("📁 Workspace: " + ws);
        workspaceLabel.setFont(subtitleFont);
        GridData wsGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        wsGd.verticalIndent = 8;
        workspaceLabel.setLayoutData(wsGd);

        statusLabel = new Label(overlayComposite, SWT.CENTER);
        statusLabel.setText("Status: Parado");
        statusLabel.setFont(boldFont);
        GridData statusGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        statusGd.verticalIndent = 12;
        statusLabel.setLayoutData(statusGd);

        errorLabel = new Label(overlayComposite, SWT.CENTER | SWT.WRAP);
        errorLabel.setText("");
        errorLabel.setFont(subtitleFont);
        errorLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
        GridData errGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        errGd.widthHint = 400;
        errorLabel.setLayoutData(errGd);
        errorLabel.setVisible(false);

        // Botões de ação rápida centralizados
        Composite btnComposite = new Composite(overlayComposite, SWT.NONE);
        GridLayout btnGrid = new GridLayout(1, false);
        btnGrid.verticalSpacing = 8;
        btnComposite.setLayout(btnGrid);
        btnComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        startButton = new Button(btnComposite, SWT.PUSH);
        startButton.setText("▶ Iniciar DeepSeek Harness");
        startButton.setFont(boldFont);
        GridData sbGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sbGd.widthHint = 260;
        sbGd.heightHint = 34;
        startButton.setLayoutData(sbGd);
        startButton.addListener(SWT.Selection, e -> DshManager.getInstance().start());

        stopButton = new Button(btnComposite, SWT.PUSH);
        stopButton.setText("🛑 Parar Processos Anteriores");
        GridData stGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        stGd.widthHint = 260;
        stGd.heightHint = 30;
        stopButton.setLayoutData(stGd);
        stopButton.addListener(SWT.Selection, e -> DshManager.getInstance().stop());

        Button consoleBtn = new Button(btnComposite, SWT.PUSH);
        consoleBtn.setText("📄 Ver Logs no Console");
        GridData cbGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        cbGd.widthHint = 260;
        cbGd.heightHint = 30;
        consoleBtn.setLayoutData(cbGd);
        consoleBtn.addListener(SWT.Selection, e -> EverywhereConsole.show());
    }

    private void createBrowserComposite(Composite parent) {
        browserComposite = new Composite(parent, SWT.NONE);
        browserComposite.setLayout(new GridLayout(1, false));

        try {
            browser = new Browser(browserComposite, SWT.NONE);
            browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        } catch (SWTError e) {
            Label err = new Label(browserComposite, SWT.WRAP);
            err.setText("Navegador web SWT não disponível no ambiente atual:\n" + e.getMessage() +
                    "\n\nUse o botão 'Abrir no Navegador' na barra de ferramentas para interagir.");
            err.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }
    }

    private void createActions() {
        startAction = new StartHarnessAction();
        stopAction = new StopHarnessAction();
        restartAction = new RestartHarnessAction();
        reloadAction = new ReloadViewAction(() -> {
            if (browser != null && !browser.isDisposed()) {
                String url = DshManager.getInstance().getActiveUrl();
                if (url != null && !url.isEmpty()) {
                    browser.setUrl(url);
                } else {
                    browser.refresh();
                }
            }
        });
        openInBrowserAction = new OpenInBrowserAction();
        openPreferencesAction = new OpenPreferencesAction();
    }

    private void contributeToActionBars() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(startAction);
        toolBarManager.add(stopAction);
        toolBarManager.add(restartAction);
        toolBarManager.add(reloadAction);
        toolBarManager.add(openInBrowserAction);
        toolBarManager.add(new org.eclipse.jface.action.Separator());
        toolBarManager.add(openPreferencesAction);
    }

    @Override
    public void onStatusChanged(final HarnessStatus status, final String url, final Integer port, final String error) {
        Display display = Display.getDefault();
        if (display == null || display.isDisposed()) return;

        display.asyncExec(() -> {
            if (mainContainer == null || mainContainer.isDisposed()) return;

            // Atualiza botões da toolbar
            if (startAction != null) startAction.updateState();
            if (stopAction != null) stopAction.updateState();

            // Atualiza labels e controles no overlay
            if (workspaceLabel != null && !workspaceLabel.isDisposed()) {
                workspaceLabel.setText("📁 Workspace: " + DshManager.getInstance().resolveWorkspaceDirectory());
            }

            if (statusLabel != null && !statusLabel.isDisposed()) {
                switch (status) {
                    case STARTING:
                        statusLabel.setText("⏳ Status: Iniciando serviço DeepSeek...");
                        statusLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
                        if (errorLabel != null) errorLabel.setVisible(false);
                        break;
                    case RUNNING:
                        statusLabel.setText("✅ Status: Em execução (" + (url != null ? url : "") + ")");
                        statusLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
                        if (errorLabel != null) errorLabel.setVisible(false);
                        break;
                    case ERROR:
                        statusLabel.setText("❌ Status: Falha na inicialização");
                        statusLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
                        if (errorLabel != null) {
                            errorLabel.setText(error != null ? error : "Erro desconhecido");
                            errorLabel.setVisible(true);
                        }
                        break;
                    case STOPPED:
                    default:
                        statusLabel.setText("⏹ Status: Parado");
                        statusLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
                        if (errorLabel != null) errorLabel.setVisible(false);
                        break;
                }
            }

            // Alternância de páginas na StackLayout
            if (status == HarnessStatus.RUNNING && url != null && !url.isEmpty()) {
                if (browser != null && !browser.isDisposed()) {
                    browser.setUrl(url);
                }
                stackLayout.topControl = browserComposite;
            } else {
                stackLayout.topControl = overlayComposite;
            }
            mainContainer.layout(true, true);
        });
    }

    @Override
    public void setFocus() {
        if (stackLayout != null && stackLayout.topControl == browserComposite && browser != null && !browser.isDisposed()) {
            browser.setFocus();
        } else if (mainContainer != null && !mainContainer.isDisposed()) {
            mainContainer.setFocus();
        }
    }

    @Override
    public void dispose() {
        DshManager.getInstance().removeListener(this);
        if (titleFont != null) titleFont.dispose();
        if (subtitleFont != null) subtitleFont.dispose();
        if (boldFont != null) boldFont.dispose();
        super.dispose();
    }
}
