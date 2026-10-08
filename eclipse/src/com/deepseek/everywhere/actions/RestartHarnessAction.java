package com.deepseek.everywhere.actions;

import com.deepseek.everywhere.Activator;
import com.deepseek.everywhere.service.DshManager;
import org.eclipse.jface.action.Action;

public class RestartHarnessAction extends Action {

    public RestartHarnessAction() {
        super("Reiniciar", Activator.getImageDescriptor("icons/restart.png"));
        setToolTipText("Reiniciar DeepSeek Harness");
    }

    @Override
    public void run() {
        DshManager.getInstance().restart();
    }
}
