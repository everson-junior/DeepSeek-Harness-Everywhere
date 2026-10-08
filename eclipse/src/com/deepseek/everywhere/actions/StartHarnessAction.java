package com.deepseek.everywhere.actions;

import com.deepseek.everywhere.Activator;
import com.deepseek.everywhere.model.HarnessStatus;
import com.deepseek.everywhere.service.DshManager;
import org.eclipse.jface.action.Action;

public class StartHarnessAction extends Action {

    public StartHarnessAction() {
        super("Iniciar", Activator.getImageDescriptor("icons/start.png"));
        setToolTipText("Iniciar DeepSeek Harness");
    }

    @Override
    public void run() {
        DshManager.getInstance().start();
    }

    public void updateState() {
        HarnessStatus status = DshManager.getInstance().getStatus();
        setEnabled(status != HarnessStatus.STARTING && status != HarnessStatus.RUNNING);
    }
}
