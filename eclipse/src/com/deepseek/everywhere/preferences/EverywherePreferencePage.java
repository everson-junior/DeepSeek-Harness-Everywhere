package com.deepseek.everywhere.preferences;

import com.deepseek.everywhere.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Página de preferências do Everywhere no Eclipse (Window -> Preferences -> Everywhere).
 */
public class EverywherePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public EverywherePreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Configurações do DeepSeek Harness Runtime (Everywhere)");
    }

    @Override
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(
                PreferenceConstants.P_AUTO_START,
                "&Iniciar automaticamente ao abrir a visualização",
                getFieldEditorParent()));

        IntegerFieldEditor portEditor = new IntegerFieldEditor(
                PreferenceConstants.P_PORT,
                "&Porta do Servidor HTTP (0 para porta livre automática):",
                getFieldEditorParent());
        portEditor.setValidRange(0, 65535);
        addField(portEditor);

        addField(new StringFieldEditor(
                PreferenceConstants.P_PROFILE,
                "&Perfil do Harness (padrão: 'web'):",
                getFieldEditorParent()));

        StringFieldEditor apiKeyEditor = new StringFieldEditor(
                PreferenceConstants.P_API_KEY,
                "&DeepSeek API Key (opcional, aceita via env):",
                getFieldEditorParent());
        apiKeyEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(apiKeyEditor);

        addField(new StringFieldEditor(
                PreferenceConstants.P_BASE_URL,
                "&URL Base da API DeepSeek:",
                getFieldEditorParent()));

        FileFieldEditor dshPathEditor = new FileFieldEditor(
                PreferenceConstants.P_CUSTOM_DSH_PATH,
                "&Caminho customizado do executável 'dsh':",
                getFieldEditorParent());
        dshPathEditor.setEmptyStringAllowed(true);
        addField(dshPathEditor);
    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
