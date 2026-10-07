plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.deepseek.everywhere"
version = project.findProperty("pluginVersion") as String? ?: "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

intellij {
    version.set("2024.1.2")
    type.set("IC")
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
        changeNotes.set("""
            Initial release of Everywhere for IntelliJ IDEA 2024.1+ (Build IC-241.17011.79):
            <ul>
              <li>Embedded hardware-accelerated JCEF browser tool window</li>
              <li>Workspace Project binding with automatic path discovery</li>
              <li>Autonomous process management with auto-recovery on port conflict (exit code 1)</li>
              <li>Toolbar controls: Start, Stop, Restart, Reload, Browser, Settings</li>
              <li>DeepSeek settings page for custom binary, API Key, Base URL, port and profiles</li>
            </ul>
        """.trimIndent())
    }

    buildSearchableOptions {
        enabled = false
    }
}
