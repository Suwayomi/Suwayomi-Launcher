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

    // Server ip and port bindings
    fun ip(): LauncherPreference<String> {
        return LauncherPreference(
            "ip",
            "ip",
            "0.0.0.0",
            settings,
            StringAdapter
        )
    }
    fun port(): LauncherPreference<Int> {
        return LauncherPreference(
            "port",
            "port",
            4567,
            settings,
            IntAdapter
        )
    }

    // Socks5 proxy
    fun socksProxyEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "socksProxyEnabled",
            "socks_enabled",
            false,
            settings,
            BooleanAdapter
        )
    }
    fun socksProxyHost(): LauncherPreference<String> {
        return LauncherPreference(
            "socksProxyHost",
            "socks_host",
            "",
            settings,
            StringAdapter
        )
    }
    fun socksProxyPort(): LauncherPreference<Int?> {
        return LauncherPreference(
            "socksProxyPort",
            "socks_port",
            null,
            settings,
            IntOrNullAdapter
        )
    }

    // WebUI
    fun webUIEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "webUIEnabled",
            "webui_enabled",
            true,
            settings,
            BooleanAdapter
        )
    }
    fun initialOpenInBrowserEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "initialOpenInBrowserEnabled",
            "open_in_browser",
            true,
            settings,
            BooleanAdapter
        )
    }
    enum class WebUIInterface(val key: String) {
        Browser("browser"),
        Electron("electron")
    }
    fun webUIInterface(): LauncherPreference<WebUIInterface> {
        return LauncherPreference(
            "webUIInterface",
            "webui_interface",
            WebUIInterface.Browser,
            settings,
            SerializableAdapter(
                serialize = { it.name },
                deserialize = { enumValueOf(it) }
            )
        )
    }
    fun electronPath(): LauncherPreference<String?> {
        return LauncherPreference(
            "electronPath",
            "electron_path",
            null,
            settings,
            StringOrNullAdapter
        )
    }

    // Authentication
    fun basicAuthEnabled(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "basicAuthEnabled",
            "basic_auth_enabled",
            false,
            settings,
            BooleanAdapter
        )
    }
    fun basicAuthUsername(): LauncherPreference<String?> {
        return LauncherPreference(
            "basicAuthUsername",
            "basic_auth_username",
            null,
            settings,
            StringOrNullAdapter
        )
    }
    fun basicAuthPassword(): LauncherPreference<String?> {
        return LauncherPreference(
            "basicAuthPassword",
            "basic_auth_password",
            null,
            settings,
            StringOrNullAdapter
        )
    }

    // Misc
    fun debugLogs(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "debugLogsEnabled",
            "debug",
            false,
            settings,
            BooleanAdapter
        )
    }
    fun systemTray(): LauncherPreference<Boolean> {
        return LauncherPreference(
            "systemTrayEnabled",
            "tray",
            true,
            settings,
            BooleanAdapter
        )
    }
    fun downloadsPath(): LauncherPreference<String?> {
        return LauncherPreference(
            "downloadsPath",
            "downloads",
            null,
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
        downloadsPath()
    ).mapNotNull { it.getProperty() }
}
