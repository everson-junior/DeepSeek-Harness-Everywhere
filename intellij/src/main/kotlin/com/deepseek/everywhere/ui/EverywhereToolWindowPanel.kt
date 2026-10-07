package com.deepseek.everywhere.ui

import com.deepseek.everywhere.action.*
import com.deepseek.everywhere.service.DshManagerService
import com.deepseek.everywhere.service.HarnessStatus
import com.deepseek.everywhere.service.HarnessStatusListener
import com.deepseek.everywhere.settings.EverywhereSettingsState
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.*

class EverywhereToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true, true), HarnessStatusListener, Disposable {
    private val cardLayout = CardLayout()
    private val contentCardPanel = JPanel(cardLayout)

    private val statusLabel = JBLabel("Parado").apply {
        foreground = JBColor.GRAY
        font = font.deriveFont(Font.PLAIN, 12f)
        border = JBUI.Borders.emptyRight(8)
    }

    private val overlayTitle = JBLabel("🌐 Everywhere", SwingConstants.CENTER).apply {
        font = font.deriveFont(Font.BOLD, 20f)
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private val overlaySubtitle = JBLabel("DeepSeek Harness Autonomous AI Agent", SwingConstants.CENTER).apply {
        font = font.deriveFont(Font.PLAIN, 12f)
        foreground = JBColor.GRAY
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private val overlayStatusText = JBLabel("", SwingConstants.CENTER).apply {
        font = font.deriveFont(Font.PLAIN, 12f)
        foreground = JBColor.RED
        alignmentX = Component.CENTER_ALIGNMENT
        isVisible = false
    }

    private val overlayWorkspaceInfo = JBLabel("", SwingConstants.CENTER).apply {
        font = font.deriveFont(Font.PLAIN, 11f)
        foreground = JBColor.DARK_GRAY
        alignmentX = Component.CENTER_ALIGNMENT
    }

    private val overlayStartBtn = JButton("▶ Iniciar DeepSeek Harness").apply {
        font = font.deriveFont(Font.BOLD, 13f)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        alignmentX = Component.CENTER_ALIGNMENT
        maximumSize = Dimension(280, 36)
        addActionListener {
            DshManagerService.getInstance(project).start()
        }
    }

    private val overlayStopBtn = JButton("🛑 Parar Processos Anteriores").apply {
        font = font.deriveFont(Font.PLAIN, 12f)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        alignmentX = Component.CENTER_ALIGNMENT
        maximumSize = Dimension(280, 32)
        addActionListener {
            DshManagerService.getInstance(project).stop()
        }
    }

    private var jbCefBrowser: JBCefBrowser? = null

    companion object {
        private const val CARD_BROWSER = "BROWSER"
        private const val CARD_OVERLAY = "OVERLAY"
    }

    init {
        setupToolbar()
        setupContent()

        val service = DshManagerService.getInstance(project)
        service.addListener(this)

        if (EverywhereSettingsState.instance.autoStart && service.status == HarnessStatus.STOPPED) {
            ApplicationManager.getApplication().invokeLater {
                service.start()
            }
        }
    }

    private fun setupToolbar() {
        val actionGroup = DefaultActionGroup().apply {
            add(StartHarnessAction())
            add(StopHarnessAction())
            add(RestartHarnessAction())
            add(ReloadWebViewAction {
                jbCefBrowser?.let { browser ->
                    val url = DshManagerService.getInstance(project).activeUrl
                    if (!url.isNullOrBlank()) {
                        browser.loadURL(url)
                    } else {
                        browser.cefBrowser.reload()
                    }
                }
            })
            add(OpenInBrowserAction())
            addSeparator()
            add(OpenSettingsAction())
        }

        val actionToolbar = ActionManager.getInstance().createActionToolbar(
            "EverywhereToolbar",
            actionGroup,
            true
        )
        actionToolbar.targetComponent = this

        val toolbarPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0)
            add(actionToolbar.component, BorderLayout.WEST)
            add(statusLabel, BorderLayout.EAST)
        }

        setToolbar(toolbarPanel)
    }

    private fun setupContent() {
        // Overlay Panel
        val overlayPanel = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            val centerBox = Box.createVerticalBox().apply {
                preferredSize = Dimension(340, 320)
                maximumSize = Dimension(380, 400)

                add(overlayTitle)
                add(Box.createVerticalStrut(6))
                add(overlaySubtitle)
                add(Box.createVerticalStrut(14))

                overlayWorkspaceInfo.text = "Projeto: ${project.name} (${project.basePath ?: "."})"
                add(overlayWorkspaceInfo)
                add(Box.createVerticalStrut(12))

                add(overlayStatusText)
                add(Box.createVerticalStrut(16))

                add(overlayStartBtn)
                add(Box.createVerticalStrut(8))
                add(overlayStopBtn)
                add(Box.createVerticalStrut(16))

                val tip = JBLabel(
                    "<html><center style='color:#777; font-size:10px;'>O agente DeepSeek Harness interage diretamente com o código e arquivos do seu projeto aberto no IntelliJ.</center></html>",
                    SwingConstants.CENTER
                ).apply {
                    alignmentX = Component.CENTER_ALIGNMENT
                }
                add(tip)
            }
            add(centerBox)
        }

        contentCardPanel.add(overlayPanel, CARD_OVERLAY)

        // Browser Panel
        if (JBCefApp.isSupported()) {
            val browser = JBCefBrowser()
            jbCefBrowser = browser
            contentCardPanel.add(browser.component, CARD_BROWSER)
        } else {
            val noJcefPanel = JPanel(GridBagLayout()).apply {
                val label = JBLabel("<html><center>JCEF (Java Chromium Embedded Framework) não está disponível neste ambiente.<br/>Use o botão <b>Abrir no Navegador</b> para interagir com o agente.</center></html>")
                add(label)
            }
            contentCardPanel.add(noJcefPanel, CARD_BROWSER)
        }

        cardLayout.show(contentCardPanel, CARD_OVERLAY)
        setContent(contentCardPanel)
    }

    override fun onStatusChanged(status: HarnessStatus, url: String?, port: Int?, error: String?) {
        ApplicationManager.getApplication().invokeLater {
            when (status) {
                HarnessStatus.RUNNING -> {
                    statusLabel.text = "Ativo (:${port ?: 3080})"
                    statusLabel.foreground = JBColor(Color(46, 125, 50), Color(78, 201, 176))

                    if (!url.isNullOrEmpty()) {
                        jbCefBrowser?.loadURL(url)
                    }
                    cardLayout.show(contentCardPanel, CARD_BROWSER)
                }
                HarnessStatus.STARTING -> {
                    statusLabel.text = "Iniciando..."
                    statusLabel.foreground = JBColor(Color(230, 81, 0), Color(220, 180, 80))

                    overlayStatusText.text = "Iniciando DeepSeek Harness runtime..."
                    overlayStatusText.foreground = JBColor(Color(230, 81, 0), Color(220, 180, 80))
                    overlayStatusText.isVisible = true
                    overlayStartBtn.isEnabled = false
                    cardLayout.show(contentCardPanel, CARD_OVERLAY)
                }
                HarnessStatus.ERROR -> {
                    statusLabel.text = "Erro"
                    statusLabel.foreground = JBColor.RED

                    overlayStatusText.text = "<html><center style='color:#f44336;'>${error ?: "Erro ao iniciar o serviço."}</center></html>"
                    overlayStatusText.isVisible = true
                    overlayStartBtn.isEnabled = true
                    overlayStartBtn.text = "🔄 Tentar Novamente"
                    cardLayout.show(contentCardPanel, CARD_OVERLAY)
                }
                HarnessStatus.STOPPED -> {
                    statusLabel.text = "Parado"
                    statusLabel.foreground = JBColor.GRAY

                    overlayStatusText.isVisible = false
                    overlayStartBtn.isEnabled = true
                    overlayStartBtn.text = "▶ Iniciar DeepSeek Harness"
                    cardLayout.show(contentCardPanel, CARD_OVERLAY)
                }
            }
        }
    }

    override fun dispose() {
        DshManagerService.getInstance(project).removeListener(this)
        jbCefBrowser?.let {
            Disposer.dispose(it)
        }
    }
}
