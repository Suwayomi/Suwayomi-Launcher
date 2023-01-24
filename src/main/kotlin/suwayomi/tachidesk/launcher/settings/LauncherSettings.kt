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
    fun ip(): Preference<String> {
        return Preference("ip", "0.0.0.0", settings, StringAdapter)
    }
    fun port(): Preference<Int> {
        return Preference("port", 4567, settings, IntAdapter)
    }

    // Socks5 proxy
    fun socksProxyEnabled(): Preference<Boolean> {
        return Preference("socks_enabled", false, settings, BooleanAdapter)
    }
    fun socksProxyHost(): Preference<String> {
        return Preference("socks_host", "", settings, StringAdapter)
    }
    fun socksProxyPort(): Preference<Int?> {
        return Preference("socks_port", null, settings, IntOrNullAdapter)
    }

    // WebUI
    fun webUIEnabled(): Preference<Boolean> {
        return Preference("webui_enabled", true, settings, BooleanAdapter)
    }
    fun initialOpenInBrowserEnabled(): Preference<Boolean> {
        return Preference("open_in_browser", true, settings, BooleanAdapter)
    }
    enum class WebUIInterface(val key: String) {
        Browser("browser"),
        Electron("electron")
    }
    fun webUIInterface(): Preference<WebUIInterface> {
        return Preference(
            "webui_interface",
            WebUIInterface.Browser,
            settings,
            SerializableAdapter(
                serialize = { it.name },
                deserialize = { enumValueOf(it) }
            )
        )
    }
    fun electronPath(): Preference<String?> {
        return Preference("electron_path", null, settings, StringOrNullAdapter)
    }

    // Authentication
    fun basicAuthEnabled(): Preference<Boolean> {
        return Preference("basic_auth_enabled", false, settings, BooleanAdapter)
    }
    fun basicAuthUsername(): Preference<String?> {
        return Preference("basic_auth_username", null, settings, StringOrNullAdapter)
    }
    fun basicAuthPassword(): Preference<String?> {
        return Preference("basic_auth_password", null, settings, StringOrNullAdapter)
    }

    // misc
    fun debugLogs(): Preference<Boolean> {
        return Preference("debug", false, settings, BooleanAdapter)
    }
    fun systemTray(): Preference<Boolean> {
        return Preference("tray", true, settings, BooleanAdapter)
    }
}
