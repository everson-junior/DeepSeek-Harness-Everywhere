package com.deepseek.everywhere.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JPanel

class EverywhereConfigurable : Configurable {
    private var mainPanel: JPanel? = null
    private val autoStartCheckBox = JBCheckBox("Iniciar o agente automaticamente ao abrir a Tool Window (Auto-Start)")
    private val portField = JBTextField()
    private val profileField = JBTextField()
    private val apiKeyField = JBPasswordField()
    private val baseUrlField = JBTextField()
    private val customDshPathField = TextFieldWithBrowseButton()

    override fun getDisplayName(): String = "Everywhere: DeepSeek Harness"

    override fun createComponent(): JComponent {
        customDshPathField.addBrowseFolderListener(
            "Selecionar Executável DeepSeek Harness (dsh)",
            "Localize o binário dsh ou dsh.cmd em seu sistema",
            null,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )

        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Caminho personalizado do DSH:"), customDshPathField, 1, false)
            .addTooltip("Deixe em branco para detecção automática no PATH/npm/pnpm padrão.")
            .addSeparator()
            .addLabeledComponent(JBLabel("Porta do servidor (0 = automática):"), portField, 1, false)
            .addLabeledComponent(JBLabel("Perfil de execução:"), profileField, 1, false)
            .addTooltip("Padrão: 'web' (outras opções incluem 'headless' ou 'tui').")
            .addSeparator()
            .addLabeledComponent(JBLabel("DeepSeek API Key:"), apiKeyField, 1, false)
            .addTooltip("Opcional se a variável de ambiente DEEPSEEK_API_KEY já estiver configurada.")
            .addLabeledComponent(JBLabel("Base URL da API:"), baseUrlField, 1, false)
            .addSeparator()
            .addComponent(autoStartCheckBox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val state = EverywhereSettingsState.instance
        var modified = autoStartCheckBox.isSelected != state.autoStart
        modified = modified || portField.text.trim() != state.port.toString()
        modified = modified || profileField.text.trim() != state.profile
        modified = modified || String(apiKeyField.password).trim() != state.apiKey
        modified = modified || baseUrlField.text.trim() != state.baseUrl
        modified = modified || customDshPathField.text.trim() != state.customDshPath
        return modified
    }

    override fun apply() {
        val state = EverywhereSettingsState.instance
        state.autoStart = autoStartCheckBox.isSelected
        state.port = portField.text.trim().toIntOrNull() ?: 0
        state.profile = profileField.text.trim().ifEmpty { "web" }
        state.apiKey = String(apiKeyField.password).trim()
        state.baseUrl = baseUrlField.text.trim().ifEmpty { "https://api.deepseek.com" }
        state.customDshPath = customDshPathField.text.trim()
    }

    override fun reset() {
        val state = EverywhereSettingsState.instance
        autoStartCheckBox.isSelected = state.autoStart
        portField.text = state.port.toString()
        profileField.text = state.profile
        apiKeyField.text = state.apiKey
        baseUrlField.text = state.baseUrl
        customDshPathField.text = state.customDshPath
    }

    override fun disposeUIResources() {
        mainPanel = null
    }
}
