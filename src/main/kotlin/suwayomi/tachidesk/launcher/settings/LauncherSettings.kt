package suwayomi.tachidesk.launcher.settings

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

class LauncherSettings {
    private val settings = PreferencesSettings(
        Preferences.userRoot().node("suwayomi/launcher")
    )

    private val serverConfig = ServerConfig.get(rootDir().get())

    // Server ip and port bindings
    fun ip(): LauncherPreference<String> {
        return LauncherPreference(
            "ip",
            "ip",
            serverConfig.ip,
            settings,
            StringAdapter
        )
    }
    fun port(): LauncherPreference<Int> {
        return LauncherPreference(
            "port",
            "port",
            serverConfig.port,
            settings,
            IntAdapter
        )
    }

    // Socks5 proxy
    fun socksProxyEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "socksProxyEnabled",
            "socks_enabled",
            serverConfig.socksProxyEnabled,
            settings,
            BooleanAdapter
        )
    }
    fun socksProxyHost(): LauncherPreference<String> {
        return LauncherPreference(
            "socksProxyHost",
            "socks_host",
            serverConfig.socksProxyHost,
            settings,
            StringAdapter
        )
    }
    fun socksProxyPort(): LauncherPreference<Int?> {
        return LauncherPreference(
            "socksProxyPort",
            "socks_port",
            serverConfig.socksProxyPort.trim().toIntOrNull(),
            settings,
            IntOrNullAdapter
        )
    }

    // WebUI
    fun webUIEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "webUIEnabled",
            "webui_enabled",
            serverConfig.webUIEnabled,
            settings,
            BooleanAdapter
        )
    }
    enum class WebUIFlavor {
        WebUI,
        Custom
    }
    fun webUIFlavor(): LauncherPreference<WebUIFlavor> {
        return LauncherPreference(
            "webUIFlavor",
            "webui_flavor",
            WebUIFlavor.valueOf(serverConfig.webUIFlavor),
            settings,
            SerializableAdapter(
                serialize = { it.name },
                deserialize = { value -> enumValueOf(value) }
            )
        )
    }
    fun initialOpenInBrowserEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "initialOpenInBrowserEnabled",
            "open_in_browser",
            serverConfig.initialOpenInBrowserEnabled,
            settings,
            BooleanAdapter
        )
    }
    enum class WebUIInterface {
        Browser,
        Electron
    }
    fun webUIInterface(): LauncherPreference<WebUIInterface> {
        return LauncherPreference(
            "webUIInterface",
            "webui_interface",
            WebUIInterface.values().first { serverConfig.webUIInterface == it.name.lowercase() },
            settings,
            SerializableAdapter(
                serialize = { it.name.lowercase() },
                deserialize = { value -> enumValueOf(value.replaceFirstChar { it.uppercase() }) }
            )
        )
    }
    fun electronPath(): LauncherPreference<String?> {
        return ElectronPreference(
            webUIInterface(),
            "electronPath",
            "electron_path",
            serverConfig.electronPath.takeUnless { it.isBlank() },
            settings,
            StringOrNullAdapter
        )
    }

    // Authentication
    fun basicAuthEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "basicAuthEnabled",
            "basic_auth_enabled",
            serverConfig.basicAuthEnabled,
            settings,
            BooleanAdapter
        )
    }
    fun basicAuthUsername(): LauncherPreference<String?> {
        return LauncherPreference(
            "basicAuthUsername",
            "basic_auth_username",
            serverConfig.basicAuthUsername.takeUnless { it.isBlank() },
            settings,
            StringOrNullAdapter
        )
    }
    fun basicAuthPassword(): LauncherPreference<String?> {
        return LauncherPreference(
            "basicAuthPassword",
            "basic_auth_password",
            serverConfig.basicAuthPassword.takeUnless { it.isBlank() },
            settings,
            StringOrNullAdapter
        )
    }

    // Misc
    fun debugLogs(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "debugLogsEnabled",
            "debug",
            serverConfig.debugLogsEnabled,
            settings,
            BooleanAdapter
        )
    }
    fun systemTray(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "systemTrayEnabled",
            "tray",
            serverConfig.systemTrayEnabled,
            settings,
            BooleanAdapter
        )
    }

    // Directories
    fun rootDir(): LauncherPreference<String?> {
        return LauncherPreference(
            "rootDir",
            "root",
            null,
            settings,
            StringOrNullAdapter
        )
    }
    fun downloadsPath(): LauncherPreference<String?> {
        return LauncherPreference(
            "downloadsPath",
            "downloads",
            serverConfig.downloadsPath.takeUnless { it.isBlank() },
            settings,
            StringOrNullAdapter
        )
    }

    fun getProperties() = listOf(
        ip(),
        port(),
        socksProxyEnabled(),
        socksProxyHost(),
        socksProxyPort(),
        webUIEnabled(),
        initialOpenInBrowserEnabled(),
        webUIInterface(),
        electronPath(),
        basicAuthEnabled(),
        basicAuthUsername(),
        basicAuthPassword(),
        debugLogs(),
        systemTray(),
        rootDir(),
        downloadsPath()
    ).mapNotNull { it.getProperty() }
}
