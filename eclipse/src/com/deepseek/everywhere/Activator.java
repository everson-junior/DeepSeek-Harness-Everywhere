package com.deepseek.everywhere;

import com.deepseek.everywhere.service.DshManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Activator principal do bundle Eclipse com.deepseek.everywhere.
 * Controla o ciclo de vida do plugin e encerramento de processos ao fechar o Eclipse.
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.deepseek.everywhere";

    private static Activator plugin;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            DshManager.getInstance().stop(true);
        } catch (Exception ignored) {
        }
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
