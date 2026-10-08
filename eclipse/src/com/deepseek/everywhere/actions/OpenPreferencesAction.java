package com.deepseek.everywhere.actions;

import com.deepseek.everywhere.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenPreferencesAction extends Action {

    public static final String PREF_PAGE_ID = "com.deepseek.everywhere.preferences.page";

    public OpenPreferencesAction() {
        super("Configurações", Activator.getImageDescriptor("icons/settings.png"));
        setToolTipText("Abrir preferências do Everywhere");
    }

    @Override
    public void run() {
        PreferencesUtil.createPreferenceDialogOn(
                Display.getDefault().getActiveShell(),
                PREF_PAGE_ID,
                new String[] { PREF_PAGE_ID },
                null
        ).open();
    }
}
