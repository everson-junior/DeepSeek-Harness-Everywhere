package com.deepseek.everywhere.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.deepseek.everywhere.settings.EverywhereSettingsState",
    storages = [Storage("EverywherePlugin.xml")]
)
class EverywhereSettingsState : PersistentStateComponent<EverywhereSettingsState> {
    var autoStart: Boolean = true
    var port: Int = 0
    var profile: String = "web"
    var apiKey: String = ""
    var baseUrl: String = "https://api.deepseek.com"
    var customDshPath: String = ""

    override fun getState(): EverywhereSettingsState = this

    override fun loadState(state: EverywhereSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: EverywhereSettingsState
            get() = ApplicationManager.getApplication().getService(EverywhereSettingsState::class.java)
    }
}
