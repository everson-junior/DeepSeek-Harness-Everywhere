package com.deepseek.everywhere.action

import com.deepseek.everywhere.icons.EverywhereIcons
import com.deepseek.everywhere.service.DshManagerService
import com.deepseek.everywhere.service.HarnessStatus
import com.deepseek.everywhere.settings.EverywhereConfigurable
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware

class StartHarnessAction : AnAction("Iniciar", "Inicia o serviço DeepSeek Harness", EverywhereIcons.START), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        DshManagerService.getInstance(project).start()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabled = false
            return
        }
        val status = DshManagerService.getInstance(project).status
        e.presentation.isEnabled = (status != HarnessStatus.STARTING && status != HarnessStatus.RUNNING)
    }
}

class StopHarnessAction : AnAction("Parar", "Interrompe o serviço DeepSeek Harness e limpa processos", EverywhereIcons.STOP), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        DshManagerService.getInstance(project).stop()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabled = false
            return
        }
        val status = DshManagerService.getInstance(project).status
        e.presentation.isEnabled = (status == HarnessStatus.RUNNING || status == HarnessStatus.STARTING)
    }
}

class RestartHarnessAction : AnAction("Reiniciar", "Reinicia o DeepSeek Harness", EverywhereIcons.RESTART), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        DshManagerService.getInstance(project).restart()
    }
}

class ReloadWebViewAction(private val reloadCallback: (() -> Unit)? = null) :
    AnAction("Recarregar", "Recarrega a página da interface DeepSeek Harness", EverywhereIcons.RELOAD), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        reloadCallback?.invoke()
    }
}

class OpenInBrowserAction : AnAction("Abrir no Navegador", "Abre o DeepSeek Harness no navegador web padrão", EverywhereIcons.BROWSER), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val url = DshManagerService.getInstance(project).activeUrl ?: "http://127.0.0.1:3080"
        BrowserLauncher.instance.open(url)
    }
}

class OpenSettingsAction : AnAction("Configurações", "Abrir configurações do Everywhere", EverywhereIcons.SETTINGS), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ShowSettingsUtil.getInstance().showSettingsDialog(project, EverywhereConfigurable::class.java)
    }
}
