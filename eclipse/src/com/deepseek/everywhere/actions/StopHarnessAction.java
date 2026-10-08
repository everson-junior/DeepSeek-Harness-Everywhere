package com.deepseek.everywhere.actions;

import com.deepseek.everywhere.Activator;
import com.deepseek.everywhere.model.HarnessStatus;
import com.deepseek.everywhere.service.DshManager;
import org.eclipse.jface.action.Action;

public class StopHarnessAction extends Action {

    public StopHarnessAction() {
        super("Parar", Activator.getImageDescriptor("icons/stop.png"));
        setToolTipText("Parar DeepSeek Harness e limpar processos órfãos");
    }

    @Override
    public void run() {
        DshManager.getInstance().stop();
    }

    public void updateState() {
        HarnessStatus status = DshManager.getInstance().getStatus();
        setEnabled(status == HarnessStatus.RUNNING || status == HarnessStatus.STARTING);
    }
}
