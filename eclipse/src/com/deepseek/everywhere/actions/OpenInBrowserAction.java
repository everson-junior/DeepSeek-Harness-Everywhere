package com.deepseek.everywhere.actions;

import com.deepseek.everywhere.Activator;
import com.deepseek.everywhere.service.DshManager;
import java.net.URL;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class OpenInBrowserAction extends Action {

    public OpenInBrowserAction() {
        super("Abrir no Navegador", Activator.getImageDescriptor("icons/browser.png"));
        setToolTipText("Abrir DeepSeek Harness no navegador web padrão");
    }

    @Override
    public void run() {
        String urlStr = DshManager.getInstance().getActiveUrl();
        if (urlStr == null || urlStr.isEmpty()) {
            urlStr = "http://127.0.0.1:3080";
        }
        try {
            IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
            IWebBrowser browser = browserSupport.getExternalBrowser();
            browser.openURL(new URL(urlStr));
        } catch (Exception e) {
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(urlStr));
                }
            } catch (Exception ignored) {}
        }
    }
}
