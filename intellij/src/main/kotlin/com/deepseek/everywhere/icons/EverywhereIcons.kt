package com.deepseek.everywhere.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object EverywhereIcons {
    @JvmField
    val TOOL_WINDOW: Icon = IconLoader.getIcon("/icons/toolWindow.svg", EverywhereIcons::class.java)

    @JvmField
    val START: Icon = IconLoader.getIcon("/icons/start.svg", EverywhereIcons::class.java)

    @JvmField
    val STOP: Icon = IconLoader.getIcon("/icons/stop.svg", EverywhereIcons::class.java)

    @JvmField
    val RESTART: Icon = IconLoader.getIcon("/icons/restart.svg", EverywhereIcons::class.java)

    @JvmField
    val RELOAD: Icon = IconLoader.getIcon("/icons/reload.svg", EverywhereIcons::class.java)

    @JvmField
    val BROWSER: Icon = IconLoader.getIcon("/icons/browser.svg", EverywhereIcons::class.java)

    @JvmField
    val SETTINGS: Icon = IconLoader.getIcon("/icons/settings.svg", EverywhereIcons::class.java)
}
