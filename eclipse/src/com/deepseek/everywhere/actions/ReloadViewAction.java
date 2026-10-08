package com.deepseek.everywhere.actions;

import com.deepseek.everywhere.Activator;
import org.eclipse.jface.action.Action;

public class ReloadViewAction extends Action {

    private final Runnable reloadCallback;

    public ReloadViewAction(Runnable reloadCallback) {
        super("Recarregar", Activator.getImageDescriptor("icons/reload.png"));
        setToolTipText("Recarregar visualização web");
        this.reloadCallback = reloadCallback;
    }

    @Override
    public void run() {
        if (reloadCallback != null) {
            reloadCallback.run();
        }
    }
}
