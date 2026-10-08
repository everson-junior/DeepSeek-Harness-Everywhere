package com.deepseek.everywhere.handlers;

import com.deepseek.everywhere.service.DshManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class StopHarnessHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        DshManager.getInstance().stop();
        return null;
    }
}
