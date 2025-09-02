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
    private val settings =
        PreferencesSettings(
            Preferences.userRoot().node("suwayomi/launcher"),
        )

    enum class WebUIFlavor {
        WebUI,
        VUI,
        Custom,
    }

    enum class WebUIInterface {
        Browser,
        Electron,
    }

    enum class WebUIChannel {
        Stable,
        Preview,
        Bundled,
    }

    enum class SortOrder {
        ASC,
        DESC,
    }

    enum class AuthMode {
        NONE,
        BASIC_AUTH,
        SIMPLE_LOGIN,
        UI_LOGIN,
    }

    enum class KoreaderSyncChecksumMethod {
        BINARY,
        FILENAME,
    }

    enum class KoreaderSyncStrategy {
        PROMPT,
        SILENT,
        SEND,
        RECEIVE,
        DISABLED,
    }

    enum class DatabaseType {
        H2,
        POSTGRESQL,
    }

    fun rootDir(): LauncherPreference<String?> =
        LauncherPreference(
            "rootDir",
            "root",
            null,
            settings,
            StringOrNullAdapter,
        )

    fun theme(): LauncherPreference<String?> =
        LauncherPreference(
            "theme",
            "theme",
            null,
            settings,
            StringOrNullAdapter,
        )

    fun getProperties() = listOf(rootDir()).mapNotNull { it.getProperty() }
}
