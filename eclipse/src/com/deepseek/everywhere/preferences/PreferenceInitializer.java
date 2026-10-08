package com.deepseek.everywhere.preferences;

import com.deepseek.everywhere.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Inicializador de valores padrão para as preferências do Everywhere.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.P_AUTO_START, true);
        store.setDefault(PreferenceConstants.P_PORT, 0);
        store.setDefault(PreferenceConstants.P_PROFILE, "web");
        store.setDefault(PreferenceConstants.P_API_KEY, "");
        store.setDefault(PreferenceConstants.P_BASE_URL, "https://api.deepseek.com");
        store.setDefault(PreferenceConstants.P_CUSTOM_DSH_PATH, "");
    }
}
